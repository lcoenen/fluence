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

package fluence

import cats.Monad
import cats.effect.{ContextShift, IO, Timer}
import fluence.crypto.KeyPair
import fluence.crypto.eddsa.Ed25519
import fluence.crypto.signature.{PubKeyAndSignature, Signature}
import fluence.kad.{Kademlia, KademliaImpl, RoutingConf}
import fluence.kad.http.{Contact, KademliaHttpClient, UriContact, UriContactOps}
import fluence.kad.protocol.{ContactAccess, Key, Node}
import fluence.kad.routing.RoutingTable
import fluence.log.Log.Aux
import cats.syntax.flatMap._
import fluence.log.{Context, Log, LogFactory}

import scala.concurrent.duration._
import scodec.bits.ByteVector

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("KademliaAPI")
class KademliaAPI(kademlia: Kademlia[IO, UriContact])(implicit log: Log[IO]) {

  @JSExport
  def join(peers: js.Array[String], numberOfNodes: Int): js.Promise[Boolean] = {
    Log[IO].scope("kad" -> "join")(
      log ⇒
        kademlia
          .join(peers.toArray.toSeq.map(p => UriContactOps.readContact.unsafe(p)), numberOfNodes)(log)
          .unsafeToFuture()
          .toJSPromise
    )
  }

  @JSExport
  def findNode(key: String, maxRequests: Int): js.Promise[kad.http.Node] = {
    val keyK = Key(ByteVector.fromValidBase58(key))
    Log[IO].scope("kad" -> "join")(
      log ⇒
        kademlia
          .findNode(keyK, maxRequests)(log)
          .map(n => {
            n.map(
                c =>
                  kad.http.Node(
                    key,
                    Contact(c.contact.host,
                            c.contact.port.toString,
                            kad.http.Signature(c.contact.signature.publicKey.value.toHex,
                                               c.contact.signature.signature.sign.toHex))
                )
              )
              .orNull
          })
          .unsafeToFuture()
          .toJSPromise
    )
  }
}

object KademliaAPI {

  @JSExportTopLevel("kademliaInit")
  def init(): js.Promise[KademliaAPI] = {
    implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
    implicit val ioTimer: Timer[IO] = IO.timer(ExecutionContext.global)

    implicit val logFactory: LogFactory[IO] = LogFactory.forPrintln[IO]()

    implicit val ca: ContactAccess[IO, UriContact] = new ContactAccess[IO, UriContact](
      pingExpiresIn = 10.seconds,
      _ ⇒ IO.pure(true),
      (contact: UriContact) ⇒ new KademliaHttpClient(contact.host, contact.port, "")
    )

    val empty = ByteVector.empty
    val signAlgo = Ed25519.signAlgo
    val kp = KeyPair.fromByteVectors(empty, empty)
    val key = Key(empty)
    val contact = UriContact("0.0.0.0", 0, PubKeyAndSignature(kp.publicKey, Signature(empty)))
    val conf = RoutingConf(10, 10, 1, 10.seconds)

    (logFactory.init("kad-js", "run") >>= { implicit log: Log[IO] ⇒
      for {
        rt <- RoutingTable[IO, IO.Par, UriContact](
          key,
          siblingsSize = 20,
          maxBucketSize = 20
        )
        kad = Kademlia[IO, IO.Par, UriContact](rt, IO(Node[UriContact](key, contact)), conf)
      } yield new KademliaAPI(kad)
    }).unsafeToFuture().toJSPromise
  }
}
