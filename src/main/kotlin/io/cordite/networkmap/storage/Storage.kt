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
package io.cordite.networkmap.storage

import io.vertx.core.Future
import io.vertx.ext.web.RoutingContext
import java.time.Duration

interface Storage<T> {
  fun clear(): Future<Unit>
  fun put(key: String, value: T): Future<Unit>
  fun get(key: String): Future<T>
  fun getOrNull(key: String): Future<T?>
  fun getKeys(): Future<List<String>>
  fun getAll(): Future<Map<String, T>>
  fun getAll(keys: List<String>) : Future<Map<String, T>>
  fun delete(key: String): Future<Unit>
  fun exists(key: String): Future<Boolean>
  fun serve(key: String, routingContext: RoutingContext, cacheTimeout: Duration)
  fun size(): Future<Int>
  fun getPage(page: Int, pageSize: Int): Future<Map<String, T>>
}