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

import fluence.crypto.KeyPair
import fluence.crypto.eddsa.Ed25519
import fluence.crypto.signature.{PubKeyAndSignature, Signature}
import scodec.bits.ByteVector

object KademliaTest extends App {
  val pk = KeyPair.Public(ByteVector.fromValidBase58("2SuMkc7ESuGk6ecywkyqK1J2tNYXni9EV6DN5xa9hHkk"))

  val sign = Signature(
    ByteVector.fromValidBase58(
      "3z4rmJeCedBcxrH7sQaMc4YnAA756wboLfuuWq3NrcNngW6YQQ72EYuCkVMKsVTS3MGqYKovnZhPqSamHnpGKwJw"
    )
  )
  val pks = PubKeyAndSignature(pk, sign)
  val c = UriContact("159.65.110.2", 25000, pks)

  val a = Ed25519.ed25519.verify(pk, sign, c.msg)

  println(pk.value.toHex)
  println(c.msg.toHex)

  val kp = Ed25519.ed25519.generateKeyPair.unsafe(None)
  val b = Ed25519.ed25519.sign(kp, c.msg)
  val b1 = Ed25519.ed25519.sign(kp, c.msg ++ ByteVector.fill(100)(1: Byte))
  println(b)
  println(b1)

  println(a)
}
