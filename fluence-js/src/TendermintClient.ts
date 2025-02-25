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

import {none, Option, some, Some} from "ts-option";
import {fromHex} from "./utils";
import * as debug from "debug";
import {RpcClient} from "./RpcClient";
import {QueryResponse, error, Result} from "./Result";
import {toByteArray} from "base64-js";

const d = debug("tendermintClient");

export interface BroadcastTxSyncResponse {
    code: number
    data: string
    log: string
    hash: string
}

function parseResponse(res: any): BroadcastTxSyncResponse {
     try {
         const bResponse = res.data.result;
         bResponse.data = fromHex(bResponse.data);
         return bResponse;
     } catch (e) {
         throw new Error(`Cannot parse the response because of an error: ${JSON.stringify(e)}\n Response: ${JSON.stringify(res)}`);
     }
}

export class TendermintClient {
    readonly client: RpcClient;
    readonly addr: string;
    readonly appId: string;

    constructor(host: string, port: number, appId: string, protocol: protocol = "http") {
        this.addr = `${protocol}://${host}:${port}`;
        this.appId = appId;
        this.client = new RpcClient(this.addr, appId);
    }

    /**
     * Sends broadcast_tx_sync operation.
     * @param payload transaction payload
     */
    broadcastTxSync(payload: string): Promise<BroadcastTxSyncResponse> {
        d("broadCastTxSync request");
        return this.client.broadcastTxSync(payload)
            .then(parseResponse);
    }

    /**
     * Sends an ABCI query.
     * @param path query parameter
     *
     * @returns `none` if there is no value, and `some` with parsed from hex value otherwise.
     */
    async abciQuery(path: string): Promise<Option<Result>> {
        d("abciQuery request");

        const abciQueryResult = await this.client.abciQuery(path);

        if (!abciQueryResult.data || !abciQueryResult.data.result || !abciQueryResult.data.result.response) {
            throw error(`Malformed response: ${JSON.stringify(abciQueryResult.data)}`);
        }

        const response = abciQueryResult.data.result.response;

        switch (response.code) {
            case undefined:
            case 0: {
                if (!response.value) {
                    throw error(`Error: no value on response: ${JSON.stringify(response)}`);
                }

                try {
                    return some(new Result(toByteArray(response.value)));
                } catch (e) {
                    throw error(`Error on parsing value from response: ${JSON.stringify(response)} err:  ${e}`);
                }
            }
            case 1: {
                throw error(`Cannot parse headers on path ${path}: ${response.info}`);
            }
            case 2: {
                throw error(`Request with path '${path}' is dropped: ${response.info}`);
            }
            case 3:
            case 4: {
                d(`Response is in pending state or not found: : ${response.info}`);
                return none;
            }
            default: {
                throw error(`unknown code ${response.code} response: ${JSON.stringify(response)}`);
            }
        }
    }
}

//todo ws is not work for now due to some strange behavior with encoding
type protocol = "http" //| "ws"
