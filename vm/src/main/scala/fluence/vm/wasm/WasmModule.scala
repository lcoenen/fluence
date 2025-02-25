/*
 * Copyright 2018 Fluence Labs Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fluence.vm.wasm

import java.lang.reflect.Modifier

import asmble.compile.jvm.MemoryBuffer
import asmble.run.jvm.Module.Compiled
import asmble.run.jvm.ScriptContext
import cats.data.EitherT
import cats.effect.LiftIO
import cats.Monad
import cats.syntax.either._
import fluence.vm.VmError.WasmVmError.{ApplyError, GetVmStateError, InvokeError}
import fluence.vm.VmError.{InitializationError, NoSuchFnError, VmMemoryError}

import scala.language.higherKinds
import scala.util.Try

/**
 * Wrapper of Wasm Module instance compiled by Asmble to Java class. Provides all functionality of Wasm modules
 * according to the Fluence protocol (invoke, parameter passing, hash computing). TODO: after removing alloc/
 * dealloc should be refactored to two modules types (extends the same trait): "master" (that has invoke method
 * and can routes call from user to "slaves") and "slave" (without invoke method that does only computation).
 *
 * @param name an optional module name (according to Wasm specification module name can be empty string (that is also
 *             "valid UTF-8") or even absent)
 * @param wasmMemory the memory of this module (please see comment in apply method to understand why it's optional now)
 * @param moduleInstance a instance of Wasm Module compiled by Asmble
 * @param allocateFunction a function used for allocation of a memory region for parameter passing
 * @param deallocateFunction a function used for deallocation of a memory region previously allocated
 *                          by allocateFunction
 * @param invokeFunction a function that represents main handler of Wasm module
 */
class WasmModule(
  private val name: Option[String],
  val wasmMemory: WasmModuleMemory,
  private val moduleInstance: Any,
  private val allocateFunction: Option[WasmFunction],
  private val deallocateFunction: Option[WasmFunction],
  private val invokeFunction: Option[WasmFunction]
) {

  def getName: Option[String] = name

  /**
   * Allocates a memory region in Wasm module of supplied size by allocateFunction.
   *
   * @param size a size of memory that need to be allocated
   */
  def allocate[F[_]: LiftIO: Monad](size: Int): EitherT[F, InvokeError, Int] =
    invokeWasmFunction(allocateFunction, Int.box(size) :: Nil)

  /**
   * Deallocates a previously allocated memory region in Wasm module by deallocateFunction.
   *
   * @param offset an address of the memory region to deallocate
   * @param size a size of memory region to deallocate
   */
  def deallocate[F[_]: LiftIO: Monad](offset: Int, size: Int): EitherT[F, InvokeError, Unit] =
    invokeWasmFunction(deallocateFunction, Int.box(offset) :: Int.box(size) :: Nil)
      .map(_ ⇒ ())

  /**
   * Invokes invokeFunction which exported from Wasm module with provided arguments.
   *
   * @param args arguments for invokeFunction
   */
  def invoke[F[_]: LiftIO: Monad](args: List[AnyRef]): EitherT[F, InvokeError, Int] =
    invokeWasmFunction(invokeFunction, args)

  /**
   * Reads [offset, offset+size) region from the module memory.
   *
   * @param offset an offset from which read should be started
   *  @param size bytes count to read
   */
  def readMemory[F[_]: Monad](offset: Int, size: Int): EitherT[F, VmMemoryError, Array[Byte]] =
    wasmMemory.readBytes(offset, size)

  /**
   * Writes array of bytes to module memory.
   *
   * @param offset an offset from which write should be started
   * @param injectedArray an array that should be written into the module memory
   */
  def writeMemory[F[_]: Monad](offset: Int, injectedArray: Array[Byte]): EitherT[F, VmMemoryError, Unit] =
    wasmMemory.writeBytes(offset, injectedArray)

  /**
   * Computes hash of all significant inner state of this Module. Now only memory is used for state hash computing;
   * other fields (such as Shadow stack, executed instruction counter, ...) should also be included after their
   * implementation.
   *
   */
  def computeStateHash[F[_]: Monad](): EitherT[F, GetVmStateError, Array[Byte]] =
    wasmMemory.computeMemoryHash()

  private def invokeWasmFunction[F[_]: LiftIO: Monad](
    wasmFn: Option[WasmFunction],
    args: List[AnyRef]
  ): EitherT[F, InvokeError, Int] =
    wasmFn.fold(
      EitherT.leftT[F, Int](
        NoSuchFnError(s"Unable to find the invoke function in module with name=$this"): InvokeError
      )
    )(
      fn ⇒
        for {
          rawResult ← fn(moduleInstance, args)

          // Despite our way of thinking about Wasm function return value type as one of (i32, i64, f32, f64) in
          // WasmModule context, there we can operate with Int (i32) values. It comes from our conventions about
          // Wasm modules design: they have to has only one export function as a user interface. It has to receive
          // and return a byte array, but since array can't be directly returns from Wasm part, It returns pointer
          // to in memory. And since Webassembly is only 32-bit now, Int(i32) is used as a pointer and return value
          // type. And after Wasm64 release, there should be additional logic to operate both with 32 and 64-bit
          // modules. TODO: fold with default value is just a temporary solution - after alloc/dealloc removal it
          // should be refactored to Either.fromOption
        } yield rawResult.fold(0)(_.intValue)
    )

  override def toString: String = name.getOrElse("<no-name>")
}

object WasmModule {

  /**
   * Creates instance for specified module.
   *
   * @param moduleDescription a Asmble description of the module
   * @param scriptContext a Asmble context for the module operation
   * @param allocationFunctionName a name of function that will be used for allocation
   * @param deallocationFunctionName a name of function that will be used for deallocation
   * @param invokeFunctionName a name of main module handler function
   */
  def apply[F[_]: Monad](
    moduleDescription: Compiled,
    scriptContext: ScriptContext,
    allocationFunctionName: String,
    deallocationFunctionName: String,
    invokeFunctionName: String,
    memoryHasher: MemoryHasher.Builder[F]
  ): EitherT[F, ApplyError, WasmModule] =
    for {

      moduleInstance ← EitherT.fromEither[F](Try(moduleDescription.instance(scriptContext)).toEither.left.map { e ⇒
        // TODO: method 'instance' can throw both an initialization error and a
        // Trap error, but now they can't be separated
        InitializationError(
          s"Unable to initialize module=${moduleDescription.getName}",
          Some(e)
        )
      })

      // TODO: patch Asmble to create `getMemory` method in all cases
      memory ← EitherT.fromEither[F](Try {
        val getMemoryMethod = moduleInstance.getClass.getMethod("getMemory")
        getMemoryMethod.invoke(moduleInstance).asInstanceOf[MemoryBuffer]
      }.toEither.leftMap { e ⇒
        InitializationError(
          s"Unable to get memory from module=${Option(moduleDescription.getName).getOrElse("<no-name>")}",
          Some(e)
        ): ApplyError
      })

      moduleMemory ← WasmModuleMemory(memory, memoryHasher).leftMap(
        e ⇒
          InitializationError(
            s"Unable to instantiate WasmModuleMemory for module=${moduleDescription.getName}",
            Some(e)
          ): ApplyError
      )
      (allocMethod, deallocMethod, invokeMethod) = moduleDescription.getCls.getDeclaredMethods.toStream
        .filter(method ⇒ Modifier.isPublic(method.getModifiers))
        .map(method ⇒ WasmFunction(method.getName, method))
        .foldLeft((Option.empty[WasmFunction], Option.empty[WasmFunction], Option.empty[WasmFunction])) {
          case (acc @ (None, _, _), m @ WasmFunction(`allocationFunctionName`, _)) ⇒
            acc.copy(_1 = Some(m))

          case (acc @ (_, None, _), m @ WasmFunction(`deallocationFunctionName`, _)) ⇒
            acc.copy(_2 = Some(m))

          case (acc @ (_, _, None), m @ WasmFunction(`invokeFunctionName`, _)) ⇒
            acc.copy(_3 = Some(m))

          case (acc, _) ⇒ acc
        }

    } yield
      new WasmModule(
        Option(moduleDescription.getName),
        moduleMemory,
        moduleInstance,
        allocMethod,
        deallocMethod,
        invokeMethod
      )

}
