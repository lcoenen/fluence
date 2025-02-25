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

import {error, ErrorResponse, Result} from "./Result";
import {TendermintClient} from "./TendermintClient";
import {Option} from "ts-option";
import {SessionConfig} from "./SessionConfig";
import * as debug from "debug";

const detailedDebug = debug("request-detailed");
const d = debug("result");

export interface ResultPromise {
    result(): Promise<Result>;
}

export class ResultError implements ResultPromise {

    private readonly message: string;

    constructor(_message: string) {
        this.message = _message;
    }

    async result(): Promise<Result> {
        throw error(this.message)
    }
}

/**
 * Class with the ability to make request periodically until an answer is available.
 */
export class ResultAwait implements ResultPromise {
    private readonly tm: TendermintClient;
    private readonly config: SessionConfig;
    private readonly targetKey: string;
    private readonly summaryKey: string;
    private canceled: boolean;
    private canceledReason: string;
    private requestResult: Promise<Result>;
    private onError: (err: ErrorResponse) => any;

    /**
     *
     * @param _tm transport to the real-time cluster
     * @param _config
     * @param _targetKey key to check restul from cluster
     * @param _summaryKey key to check session info from cluster
     * @param _onError callback on error
     */
    constructor(_tm: TendermintClient, _config: SessionConfig, _targetKey: string,
                _summaryKey: string, _onError: (err: ErrorResponse) => void) {
        this.tm = _tm;
        this.config = _config;
        this.targetKey = _targetKey;
        this.summaryKey = _summaryKey;
        this.onError = _onError;
        this.canceled = false;
    }

    /**
     * Creates promise that will wait `ms` milliseconds.
     *
     * @param ms milliseconds to wait
     */
    private async sleep(ms: number) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }

    /**
     * Periodically checks the node of the real-time cluster for the presence of a result.
     * If the result is already obtained, return it without new calculations.
     *
     */
    async result(): Promise<Result> {

        d("start to get result");

        if (this.requestResult === undefined) {

            const path = this.targetKey;

            let pr = this.checkResultPeriodically(path, this.config.requestsPerSec,
                this.config.checkSessionTimeout, this.config.requestTimeout)
                .finally(() => {
                    detailedDebug("invocation completed");
                    d("result received");
                });

            pr.catch(this.onError);

            this.requestResult = pr;

            return pr;
        } else {
            return this.requestResult
        }
    }

    /**
     * Sends request for a result and parse it.
     * @param path
     * @returns `none` if there is no result, `some` if result appeared and throws an error if result is an error
     */
    private async checkResult(path: string): Promise<Option<Result>> {
        return await this.tm.abciQuery(path);
    }

    /**
     * Checks the result until it appears or until an error occurs or the session is closed.
     * @param path address to check result
     * @param requestsPerSec the frequency of requests to check per second
     * @param responseTimeoutSec the time after which it will check the session for activity too
     * @param requestTimeout the time after which the error occurs if the result has not yet been received
     * @returns result or error if some error occurred or session is closed
     */
    private async checkResultPeriodically(path: string, requestsPerSec: number, responseTimeoutSec: number,
                                          requestTimeout: number): Promise<Result> {
        for(var _i = 0; _i < requestsPerSec * requestTimeout; _i++) {

            detailedDebug("check result. Attempt number: " + _i);

            // checking result was canceled outside
            if (this.canceled) {
                throw error(`The request was canceled. Cause: ${this.canceledReason}`)
            }

            let optionResult = await this.checkResult(path);
            detailedDebug("result received");

            // if result exists, return it
            if (optionResult.nonEmpty) {
                return optionResult.get
            }

            detailedDebug("result is empty");

            // wait for next check
            await this.sleep(1000 / requestsPerSec);
        }

        throw error(`The request was timouted after ${requestTimeout} seconds.`)
    }

    /**
     * Cancels result checking.
     */
    cancel(reason: string) {
        this.canceled = true;
        this.canceledReason = reason;
    }
}
