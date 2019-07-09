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
import fluence.codec.{CodecError, PureCodec}
import fluence.codec.PureCodec.liftFuncEither
import fluence.crypto.eddsa.Ed25519
import cats.implicits._
import fluence.kad.http.UriContact.{~~>, pkWithSignatureCodec}

import scala.util.Try

object UriContactOps {

  /**
   * Read the contact, performing all the formal validations on the way. Note that signature is not checked
   */
  val readContact: String ~~> UriContact = liftFuncEither { (uri: String) =>
    val arr = uri.replaceFirst("fluence://", "").split("@")

    val b = for {
      (key, hostPort) <- {
        if (arr.size != 2) {
          Left(CodecError("User info must be provided"))
        } else {
          Right((arr(0), arr(1)))
        }
      }
      pks <- {
        Right(key).map(_.split(':')).flatMap {
          case Array(a, b) ⇒ Right((a, b))
          case _ ⇒ Left(CodecError("User info must be in pk:sign form"))
        }
      }
      (host, port) <- {
        Right(hostPort).map(_.split(':')).flatMap {
          case Array(a, b) ⇒ Right((a, b))
          case _ ⇒ Left(CodecError("Host and port must be presented"))
        }
      }
    } yield {
      println("host: " + host)
      println("port: " + port)
      println("pks: " + pks)
      val pk = pkWithSignatureCodec.direct.runEither[Id](pks)
      val pkk = pk.right.get
      val msg = UriContact(host, port.toShort, pkWithSignatureCodec.direct.unsafe(pks))
      val result = Ed25519.ed25519.verify[Try](pkk.publicKey, pkk.signature, msg.msg)
      println("result: " + result)
      println("msg: " + msg.toString)
      msg
    }

    b
  }
}
