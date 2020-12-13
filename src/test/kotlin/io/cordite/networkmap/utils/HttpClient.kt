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

import io.bluebank.braid.core.http.failed
import io.vertx.core.Future
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientResponse
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import net.corda.core.serialization.serialize
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType

fun HttpClient.futurePost(uri: String, json: JsonObject, vararg headers: Pair<String, String>): Future<Buffer> {
  return futurePost(uri, json.encode(), *headers)
}

fun HttpClient.futurePost(uri: String, body: String, vararg headers: Pair<String, String>): Future<Buffer> {
  return futureRequest(HttpMethod.POST, uri, body, *headers)
}

fun HttpClient.futurePostRaw(uri: String, body: Any, vararg headers: Pair<String, String>): Future<HttpClientResponse> {
	return futureRequestRaw(HttpMethod.POST, uri, body, *headers)
}

fun HttpClient.futurePost(uri: String, body: Buffer, vararg headers: Pair<String, String>): Future<Buffer> {
  return futureRequest(HttpMethod.POST, uri, body, *headers)
}

fun HttpClient.futurePut(uri: String, body: String, vararg headers: Pair<String, String>): Future<Buffer> {
  return futureRequest(HttpMethod.PUT, uri, body, *headers)
}

fun HttpClient.futurePut(uri: String, body: Buffer, vararg headers: Pair<String, String>): Future<Buffer> {
  return futureRequest(HttpMethod.PUT, uri, body, *headers)
}

fun HttpClient.futurePut(uri: String, body: JsonObject, vararg headers: Pair<String, String>): Future<Buffer> {
  return futureRequest(HttpMethod.PUT, uri, body.encode(), *headers)
}

fun HttpClient.futureGet(uri: String, vararg headers: Pair<String, String>): Future<Buffer> {
  return futureRequest(HttpMethod.GET, uri, "", *headers)
}

fun HttpClient.futureDelete(uri: String, vararg headers: Pair<String, String>): Future<Buffer> {
  return futureRequest(HttpMethod.DELETE, uri, "", *headers)
}

fun HttpClient.futureRequest(method: HttpMethod, uri: String, body: Buffer, vararg headers: Pair<String, String>): Future<Buffer> {
  val result = Future.future<Buffer>()
  @Suppress("DEPRECATION")
  this.request(method, uri) { response ->
    if (response.failed) {
      result.fail(response.statusMessage())
    } else {
      response.bodyHandler { buffer ->
        result.complete(buffer)
      }
    }
  }
    .putHeader(HttpHeaders.CONTENT_LENGTH, body.length().toString())
    .putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
    .apply {
      headers.forEach {
        putHeader(it.first, it.second)
      }
    }
    .exceptionHandler {
      result.fail(it)
    }
    .end(body)
  return result
}

fun HttpClient.futureRequestRaw(method: HttpMethod, uri: String, body: Any, vararg headers: Pair<String, String>): Future<HttpClientResponse> {
  val result = Future.future<HttpClientResponse>()
  @Suppress("DEPRECATION")
  this.request(method, uri) { response ->
    if (response.failed) {
      result.fail(response.statusMessage())
    } else {
      result.complete(response)
    }
  }
    .putHeader(HttpHeaders.CONTENT_LENGTH, body.serialize().bytes.size.toString())
    .putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM)
    .apply {
      headers.forEach {
        putHeader(it.first, it.second)
      }
    }
    .exceptionHandler {
      result.fail(it)
    }
    .end(Buffer.buffer(body.serialize().bytes))
  return result
}

fun HttpClient.futureRequest(method: HttpMethod, uri: String, body: String, vararg headers: Pair<String, String>): Future<Buffer> {
  val result = Future.future<Buffer>()
  @Suppress("DEPRECATION")
  this.request(method, uri)
  { response ->
    if (response.failed) {
      result.fail(response.statusMessage())
    } else {
      response.bodyHandler { buffer ->
        result.complete(buffer)
      }
    }
  }
    .putHeader(HttpHeaders.CONTENT_LENGTH, body.length.toString())
    .putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
    .apply {
      headers.forEach {
        putHeader(it.first, it.second)
      }
    }
    .exceptionHandler {
      result.fail(it)
    }
    .end(body)
  return result
}

