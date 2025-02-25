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
import java.nio.ByteOrder

import asmble.compile.jvm.MemoryBuffer
import fluence.vm.utils.safelyRunThrowable
import cats.Monad
import cats.data.EitherT
import fluence.vm.VmError.VmMemoryError
import fluence.vm.VmError.WasmVmError.GetVmStateError

import scala.language.higherKinds

final case class WasmModuleMemory private (memory: MemoryBuffer, memoryHasher: MemoryHasher) {

  /**
   * Reads [offset, offset+size) region from the memory.
   *
   * @param offset offset from which read should be started
   * @param size bytes count to read
   */
  def readBytes[F[_]: Monad](
    offset: Int,
    size: Int
  ): EitherT[F, VmMemoryError, Array[Byte]] =
    safelyRunThrowable(
      {
        // need a shallow ByteBuffer copy to avoid modifying the original one used by Asmble
        val wasmMemoryView = memory.duplicate()
        wasmMemoryView.order(ByteOrder.LITTLE_ENDIAN)

        val resultBuffer = new Array[Byte](size)
        // sets limit to capacity
        wasmMemoryView.clear()
        wasmMemoryView.position(offset)
        wasmMemoryView.get(resultBuffer)
        resultBuffer
      },
      e ⇒
        VmMemoryError(
          s"Reading from offset=$offset $size bytes failed",
          Some(e)
      )
    )

  /**
   * Writes array of bytes to memory.
   *
   * @param offset offset from which write should be started
   * @param injectedArray array that should be injected into the module memory
   */
  def writeBytes[F[_]: Monad](
    offset: Int,
    injectedArray: Array[Byte]
  ): EitherT[F, VmMemoryError, Unit] =
    safelyRunThrowable(
      {
        // need a shallow ByteBuffer copy to avoid modifying the original one used by Asmble
        val wasmMemoryView = memory.duplicate()

        wasmMemoryView.position(offset)
        wasmMemoryView.put(injectedArray)
        ()
      },
      e ⇒ VmMemoryError(s"Writing to $offset failed", Some(e))
    )

  /**
   * Computes and returns hash of memory.
   *
   */
  def computeMemoryHash[F[_]: Monad](): EitherT[F, GetVmStateError, Array[Byte]] = memoryHasher.computeMemoryHash()

}

object WasmModuleMemory {

  def apply[F[_]: Monad](
    memory: MemoryBuffer,
    memoryHasher: MemoryHasher.Builder[F]
  ): EitherT[F, GetVmStateError, WasmModuleMemory] =
    for {
      memoryHasher <- memoryHasher(memory)
    } yield new WasmModuleMemory(memory, memoryHasher)

}
