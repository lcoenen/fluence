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

import scala.annotation.meta.field
import scala.scalajs.js.annotation.JSExport

@JSExport
case class NodeJS(@(JSExport @field) key: String, @(JSExport @field) contact: Contact)

@JSExport
case class Contact(@(JSExport @field) host: String,
                   @(JSExport @field) port: String,
                   @(JSExport @field) signature: Signature)

@JSExport
case class Signature(@(JSExport @field) publicKey: String, @(JSExport @field) signature: String)
