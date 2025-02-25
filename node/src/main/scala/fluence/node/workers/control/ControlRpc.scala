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

package fluence.node.workers.control

import cats.data.EitherT
import cats.effect.Sync
import com.softwaremill.sttp._
import fluence.effects.tendermint.block.history.Receipt
import fluence.node.workers.status.HttpStatus
import scodec.bits.ByteVector

import scala.language.higherKinds

/**
 * RPC channel from node to worker
 */
abstract class ControlRpc[F[_]] {

  /**
   * Request worker to send a vote to Tendermint for removal of a validator
   *
   * @param key Public key of the Tendermint validator
   */
  def dropPeer(key: ByteVector): EitherT[F, ControlRpcError, Unit]

  /**
   * Request current worker status
   *
   * @return Currently if method returned without an error, worker is considered to be healthy
   */
  def status: F[HttpStatus[Unit]]

  /**
   * Requests worker to stop
   */
  def stop: EitherT[F, ControlRpcError, Unit]

  /**
   * Send block manifest receipt, so state machine can use it for app hash calculation
   */
  def sendBlockReceipt(receipt: Receipt): EitherT[F, ControlRpcError, Unit]

  /**
   * Retrieves vm hash from state machine, required for block manifest uploading
   */
  def getVmHash(height: Long): EitherT[F, ControlRpcError, ByteVector]
}

object ControlRpc {

  /**
   * Creates a ControlRPC instance. Currently [[HttpControlRpc]] is used.
   *
   * @param hostname Hostname to send control requests
   * @param port Port to send control requests
   * @return Instance implementing ControlRPC interface
   */
  def apply[F[_]: Sync](hostname: String, port: Short)(
    implicit s: SttpBackend[EitherT[F, Throwable, ?], Nothing]
  ): ControlRpc[F] =
    new HttpControlRpc[F](hostname, port)
}
