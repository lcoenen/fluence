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

import cats.effect.{ContextShift, IO, Timer}
import fluence.KademliaAPI
import fluence.crypto.KeyPair
import fluence.crypto.eddsa.Ed25519
import fluence.crypto.hash.JsCryptoHasher
import fluence.crypto.signature.{PubKeyAndSignature, Signature}
import fluence.kad.http.facade.Axios
import fluence.log.{Log, LogFactory}
import scodec.bits.ByteVector

import scala.scalajs.js.JSConverters._
import scala.concurrent.ExecutionContext.Implicits.global

object KademliaTest {

  def main(args: Array[String]): Unit = {

    implicit val ioTimer: Timer[IO] = IO.timer(global)
    implicit val ioShift: ContextShift[IO] = IO.contextShift(global)

    implicit val log: Log[IO] = LogFactory.forPrintln[IO]().init("kad", "example").unsafeRunSync()

    val a = KademliaAPI
      .init()
      .toFuture
      .flatMap { api =>
        api
          .join(
            Array(
              "fluence://J5kMEbCAV1HFZnsTjDqyvkJcgAX4zq5A2BCm6eQjtsLb:2KBEng9GZcNNUMHXBmohCaqbnhTsB8K6cdDP7EhhZJuHoaPnMvdUhGDqhQmRqhgCX7d9u1nr1WswKXBCYc1iUSx2@172.17.0.1:5678",
              "fluence://ARQqFPuW7asoZtw5JGB32fXvgJzHK9E3HEYoBHh1dKAV:3KVKDyDu99uwZ7UocE1DH5sDY3eYJXfrqHLrkkDDtYCBbvTJ8vmvjqVMFZgRohcVmpzsyrQ3nqp2ARW3YEUby3rY@207.154.240.52:25000",
              "fluence://5hEpwXEdBcofveXdZBLwhzD5vDcS9mPjj5AUfyhnKQKy:cLtu1J6t6vkuCwtfBCdG42W4dRCjSQJcak3yb28ztPcvroCdE9dG6AhUADER5cVB3E16jGGxbbu8vDXR2LmgMw5@172.17.0.1:5678",
              "fluence://Bx274w34MLanytDgxjnQ7a35eRGuao9XAZTC9KzYNzzy:5HDWMdpci62LGW1dbBZoXYVM176dktLtjUdvDY9yS4j261GZT3gMwgFBhUmdBc73WZGLiMPnH2uXqnUYQVLebFPF@46.101.221.231:25000",
              "fluence://93s4nPLHxNK1zBh6QuFvovo4koWQRG1sTo3z9hPKSorn:4fTNbWocKVLsMJUNSG8BSaxJXrxAcMotcGcXEg2cy4KY3FXR3q8ZGa2KV7VZQrr4UUzgs2NmGAFtc8p27KprBLkc@172.17.0.1:5678",
              "fluence://AeHUZB21QmKYzAdgHCUixXeQbvehCx21wzeoNqCM7iuj:4ALBccAZGPL96oneQpKawGMfbEh4FessSGKRbD7gMLJzVoUab3uWZZ8nGGZvcfWEEYiyQSm9FqmWvfBUsTGrqqSf@207.154.223.200:25000",
              "fluence://D9wnaS3nqRGbT4boLgYrfGtoVw3XceUx6ACwQPWE2Kty:5QMzMbiykrBkePWkFAUfEzV6wS4dv1kbQdeV6cXbdHiCxVajXFLgrug5sTMtYYyVPw8NKYPpdfYo7ziJi3BugwSv@209.97.191.196:25000",
              "fluence://C6caVv7UfNoqeg3zFEANZ7gK9vtGfg61osiwin1LoAki:31M2GUZ4btrAWGeQcaQS5aRDWUzCaK38AqGen6Dm4Q9GfXxjSd8uopohf3U3Zhab9c8FUmMRWxXHtNx8E1SyT5Wu@165.22.142.185:25000",
              "fluence://DNsrJL3xHR2NAmsAQpQNXvCzYoDzTNBQKb6nDtPL7rXy:2EAfjkmocNEUvHhET6Nwq94SALkKHasT9W8eZ7KnTh7Tf8TE7BzkFW8gEqK1kXUwXn2Qo1By9DFgHkQzdgX4Ts2C@172.17.0.1:5678",
              "fluence://2SuMkc7ESuGk6ecywkyqK1J2tNYXni9EV6DN5xa9hHkk:3z4rmJeCedBcxrH7sQaMc4YnAA756wboLfuuWq3NrcNngW6YQQ72EYuCkVMKsVTS3MGqYKovnZhPqSamHnpGKwJw@159.65.110.2:25000"
            ).toJSArray,
            10
          )
          .toFuture
      }
      .map(r => println(r))

    /*val pk = KeyPair.Public(ByteVector.fromValidBase58("2SuMkc7ESuGk6ecywkyqK1J2tNYXni9EV6DN5xa9hHkk"))

    val sign = Signature(
      ByteVector.fromValidBase58(
        "3z4rmJeCedBcxrH7sQaMc4YnAA756wboLfuuWq3NrcNngW6YQQ72EYuCkVMKsVTS3MGqYKovnZhPqSamHnpGKwJw"
      )
    )
    val pks = PubKeyAndSignature(pk, sign)
    val c = UriContact("159.65.110.2", 25000, pks)

    println(pk.value.toHex)
    println(c.msg.toHex)

    val signer = new Ed25519(None)

    val kp = signer.generateKeyPair.unsafe(None)
    val a = signer.sign(kp, c.msg)
    val b = signer.sign(kp, c.msg ++ ByteVector.fill(100)(1: Byte))
    println(a)
    println(b)*/

    /*val k = new KademliaHttpClient("localhost", 1234, "")
    k.ping().value.unsafeToFuture().map(r => println(r))*/

//    println(Axios.post("http://google.com/"))
  }
}
