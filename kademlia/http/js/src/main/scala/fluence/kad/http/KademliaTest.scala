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
import fluence.log.{Log, LogFactory}

import scala.concurrent.ExecutionContext.Implicits.global

object KademliaTest {

  def main(args: Array[String]): Unit = {

    implicit val ioTimer: Timer[IO] = IO.timer(global)
    implicit val ioShift: ContextShift[IO] = IO.contextShift(global)

    implicit val log: Log[IO] = LogFactory.forPrintln[IO]().init("kad", "example").unsafeRunSync()

    val k = new KademliaHttpClient("localhost", 1234, "")
    println(k.ping().value)
  }
}
