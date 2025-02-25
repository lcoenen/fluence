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

package fluence.statemachine.vm

import cats.Monad
import cats.data.EitherT
import cats.effect.LiftIO
import fluence.statemachine.error.{StateMachineError, VmRuntimeError}
import fluence.vm.{VmError, WasmVm}
import scodec.bits.ByteVector

import scala.language.higherKinds

/**
 * Invokes operations on provided VM.
 *
 * @param vm VM instance used to make function calls and to retrieve state
 */
class WasmVmOperationInvoker[F[_]: LiftIO](vm: WasmVm)(implicit F: Monad[F]) extends VmOperationInvoker[F] {

  /**
   * Invokes the provided invocation description using the underlying VM.
   *
   * @param arg an argument for Wasm VM module main handler
   * @return either successful invocation's result or failed invocation's error
   */
  def invoke(arg: Array[Byte]): EitherT[F, StateMachineError, Array[Byte]] =
    vm
    // by our name conventional a master Wasm module in VM doesn't have name
      .invoke(None, arg)
      .leftMap(WasmVmOperationInvoker.convertToStateMachineError)

  /**
   * Obtains the current state hash of VM.
   *
   */
  def vmStateHash(): EitherT[F, StateMachineError, ByteVector] =
    vm.getVmState.leftMap(WasmVmOperationInvoker.convertToStateMachineError)
}

object WasmVmOperationInvoker {

  /**
   * Converts [[VmError]] to [[StateMachineError]]
   * TODO: handle different error types separately; possibly logging is required here.
   *
   * @param vmError error returned from VM
   */
  def convertToStateMachineError(vmError: VmError): StateMachineError =
    VmRuntimeError(vmError.getClass.getSimpleName, vmError.getMessage, vmError)
}
