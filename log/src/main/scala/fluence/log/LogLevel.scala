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

package fluence.log

import scala.language.implicitConversions

// TODO: Remove this loglevel, use Log.Level from fluence.log everywhere
object LogLevel extends Enumeration {
  type LogLevel = Value

  val OFF = Value("OFF")
  val ERROR = Value("ERROR")
  val WARN = Value("WARN")
  val INFO = Value("INFO")
  val DEBUG = Value("DEBUG")
  val TRACE = Value("TRACE")

  def fromString(s: String): Option[Value] = values.find(_.toString.equalsIgnoreCase(s))

  implicit def toLogLevel(l: LogLevel): Log.Level = {
    l match {
      case `OFF`   => Log.Off
      case `ERROR` => Log.Error
      case `WARN`  => Log.Warn
      case `INFO`  => Log.Info
      case `DEBUG` => Log.Debug
      case `TRACE` => Log.Trace
    }
  }
}
