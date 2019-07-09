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

package fluence.kad.http.facade

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobal, JSImport}

@js.native
@JSImport("axios", JSImport.Namespace)
object Axios extends js.Object {
  var defaults: js.Any = js.native

  def get(url: String, config: js.Any = null): js.Promise[Response] = js.native
  def delete(url: String, config: js.Any = null): js.Promise[Response] = js.native
  def head(url: String, config: js.Any = null): js.Promise[Response] = js.native
  def options(url: String, config: js.Any = null): js.Promise[Response] = js.native
  def post(url: String, config: js.Any = null): js.Promise[Response] = js.native
  def put(url: String, config: js.Any = null): js.Promise[Response] = js.native
  def patch(url: String, config: js.Any = null): js.Promise[Response] = js.native
}

@js.native
@JSGlobal
class Response extends js.Object {
  val data: js.Dynamic = js.native
  val status: Int = js.native
  val statusText: String = js.native
  val headers: js.Dictionary[String] = js.native
  val config: js.Any = js.native
  val request: js.Any = js.native
}
