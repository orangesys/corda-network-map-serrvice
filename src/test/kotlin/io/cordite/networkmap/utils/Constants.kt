/**
 *   Copyright 2018, Cordite Foundation.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package io.cordite.networkmap.utils

import net.corda.core.utilities.millis
import java.time.Duration

val CACHE_TIMEOUT = 10.millis
val NETWORK_PARAM_UPDATE_DELAY : Duration = 10.millis
const val SAMPLE_INPUTS = "test-data/inputs/"
const val SAMPLE_NODES = "test-data/nodeinfos/"
const val DEFAULT_NETWORK_MAP_ROOT = "/"
const val ADMIN_NAME = ""
const val ADMIN_USER_NAME = "sa"
const val ADMIN_PASSWORD = ""
const val ADMIN_REST_ROOT = "/admin/api"
const val DEFAULT_HOST = "127.0.0.1"
const val NETWORK_MAP_ROOT = "/network-map"

