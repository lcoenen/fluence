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

package fluence.kad.http

import cats.Id
import cats.data.EitherT
import cats.effect.IO
import cats.syntax.either._
import fluence.crypto.Crypto
import fluence.crypto.eddsa.Ed25519
import fluence.kad.http.facade.Axios
import fluence.kad.protocol.{KademliaRpc, Key, Node}
import fluence.kad.{KadRemoteError, KadRpcError}
import fluence.log.Log
import io.circe.scalajs._
import io.circe.{Decoder, DecodingFailure}

import scala.language.higherKinds

class KademliaHttpClient(hostname: String, port: Short, auth: String) extends KademliaRpc[IO, UriContact] {

  private val signAlgo = Ed25519.signAlgo

  implicit val readNode: Crypto.Func[String, Node[UriContact]] =
    UriContact.readNode(signAlgo.checker)

  private implicit val decodeNode: Decoder[Node[UriContact]] =
    _.as[String].flatMap(readNode.runEither[Id](_).leftMap(ce ⇒ DecodingFailure(ce.message, Nil)))

  /**
   * Ping the contact, get its actual Node status, or fail.
   */
  override def ping()(implicit log: Log[IO]): EitherT[IO, KadRpcError, Node[UriContact]] = {
    val a: EitherT[IO, Throwable, Node[UriContact]] = for {
      resp <- EitherT(IO.fromFuture(IO(Axios.get(s"http://$hostname:$port/kad/ping").toFuture)).attempt)
      b <- EitherT.fromEither[IO](decodeJs[Node[UriContact]](resp.data))
    } yield b
    a.leftMap(t => KadRemoteError("Error on ping", t))
  }

  /**
   * Perform a local lookup for a key, return K closest known nodes.
   *
   * @param key Key to lookup
   */
  override def lookup(key: Key, neighbors: Int)(
    implicit log: Log[IO]
  ): EitherT[IO, KadRpcError, Seq[Node[UriContact]]] = {
    val a = for {
      resp <- EitherT(
        IO.fromFuture(IO(Axios.get(s"http://$hostname:$port/kad/lookup?key=${key.asBase58}&n=$neighbors").toFuture))
          .attempt
      )
      b <- EitherT.fromEither[IO](decodeJs[Seq[Node[UriContact]]](resp.data))
    } yield b
    a.leftMap(t => KadRemoteError("Error on lookup", t))
  }

  /**
   * Perform a local lookup for a key, return K closest known nodes, going away from the second key.
   *
   * @param key Key to lookup
   */
  override def lookupAway(key: Key, moveAwayFrom: Key, neighbors: Int)(
    implicit log: Log[IO]
  ): EitherT[IO, KadRpcError, Seq[Node[UriContact]]] = {
    val a = for {
      resp <- EitherT(
        IO.fromFuture(
            IO(
              Axios
                .get(
                  s"http://$hostname:$port/kad/lookup?key=${key.asBase58}&n=$neighbors&awayFrom=${moveAwayFrom.asBase58}"
                )
                .toFuture
            )
          )
          .attempt
      )
      b <- EitherT.fromEither[IO](decodeJs[Seq[Node[UriContact]]](resp.data))
    } yield b
    a.leftMap(t => KadRemoteError("Error on lookupAway", t))
  }
}
