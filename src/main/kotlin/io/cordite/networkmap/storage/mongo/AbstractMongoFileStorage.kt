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
package io.cordite.networkmap.storage.mongo

import com.mongodb.MongoGridFSException
import com.mongodb.client.model.Filters
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.gridfs.GridFSBuckets
import io.bluebank.braid.core.async.mapUnit
import io.bluebank.braid.core.async.toFuture
import io.cordite.networkmap.serialisation.serializeOnContext
import io.cordite.networkmap.storage.Storage
import io.cordite.networkmap.storage.mongo.rx.toObservable
import io.cordite.networkmap.storage.mongo.serlalisation.asAsyncOutputStream
import io.cordite.networkmap.storage.mongo.serlalisation.toAsyncOutputStream
import io.cordite.networkmap.utils.all
import io.cordite.networkmap.utils.catch
import io.cordite.networkmap.utils.onSuccess
import io.netty.handler.codec.http.HttpHeaderValues
import io.vertx.core.Future
import io.vertx.core.Future.succeededFuture
import io.vertx.core.http.HttpHeaders
import io.vertx.ext.web.RoutingContext
import net.corda.core.utilities.loggerFor
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.time.Duration

abstract class AbstractMongoFileStorage<T : Any>(val client: MongoClient, dbName: String, private val bucketName: String) : Storage<T> {
  companion object {
    private val log = loggerFor<AbstractMongoFileStorage<*>>()
  }

  private val db = client.getDatabase(dbName)
  private var bucket = GridFSBuckets.create(db, bucketName)

  override fun clear(): Future<Unit> {
    return bucket.drop().toFuture()
      .onSuccess { bucket = GridFSBuckets.create(db, bucketName) }
      .mapUnit()
  }

  override fun put(key: String, value: T): Future<Unit> {
    val bytes = serialize(value)
    val stream = bucket.openUploadStream(key)
    return stream.write(bytes).toFuture()
      .compose { stream.close().toFuture() }
      .onSuccess {
        log.trace("wrote file $key in bucket $bucketName")
      }
      .mapUnit()
  }

  override fun get(key: String): Future<T> {
    return ByteArrayOutputStream().use { arrayStream ->
      bucket.downloadToStream(key, arrayStream.toAsyncOutputStream()).toFuture()
        .map {
          deserialize(arrayStream.toByteArray())
        }
    }
  }

  override fun getOrNull(key: String): Future<T?> {
    return exists(key)
      .compose { exists ->
        when {
          exists -> get(key)
          else -> succeededFuture()
        }
      }
  }

  override fun getKeys(): Future<List<String>> {
    return bucket.find()
      .toObservable()
      .map { it.filename }
      .toList()
      .toFuture<List<String>>()
  }
  
  override fun size(): Future<Int> {
    return bucket.find()
      .toObservable()
      .count()
      .toFuture()
  }

  override fun getAll(keys: List<String>): Future<Map<String, T>> {
    return keys.map { key ->
      get(key).map { key to it }
    }
      .all()
      .map { pairs ->
        pairs.toMap()
      }
  }

  override fun getAll(): Future<Map<String, T>> {
    // nominal implementation - very slow - considering speeding up
    return getKeys()
      .compose { keys ->
        getAll(keys)
      }
  }

  override fun getPage(page: Int, pageSize: Int): Future<Map<String, T>> {
    return bucket.find().skip(pageSize * (page - 1)).limit(pageSize)
      .toObservable()
      .map { it.filename }
      .toList()
      .toFuture<List<String>>()
      .compose { keys ->
        keys.map { key ->
          get(key).map { key to it }
        }.all()
      }
      .map { pairs ->
        pairs.toMap()
      }
  }

  override fun delete(key: String): Future<Unit> {
    return bucket.find(Filters.eq("filename", key)).first().toFuture()
      .compose { fileDescriptor ->
        when (fileDescriptor) {
          null -> succeededFuture(Unit)
          else -> {
            bucket.delete(fileDescriptor.objectId).toFuture().mapUnit()
          }
        }
      }
  }

  override fun exists(key: String): Future<Boolean> {
    return bucket.find(Filters.eq("filename", key)).first().toFuture()
      .map { it != null }
  }

  override fun serve(key: String, routingContext: RoutingContext, cacheTimeout: Duration) {
    routingContext.response().apply {
      isChunked = true
      putHeader(HttpHeaders.CACHE_CONTROL, "max-age=${cacheTimeout.seconds}")
      putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_OCTET_STREAM)
    }

    bucket.downloadToStream(key, routingContext.asAsyncOutputStream()).toFuture()
      .onSuccess {
        // NB: we need to do this because the mongo reactive streams driver doesn't respect the reactive streams protocol
        // and never calls the close method on the output stream! 👏👏👏
        if (!routingContext.response().ended()) {
          routingContext.response().end()
        }
      }
      .catch { error ->
        when (error) {
          is MongoGridFSException -> {
            log.error("failed to find file for $key from bucket $bucketName", error)
            if (!routingContext.response().ended()) {
              routingContext.response().setStatusCode(404).setStatusMessage("file not found").end()
            }
          }
          else -> {
            log.error("failed to serve request for $key from bucket $bucketName", error)
            if (!routingContext.response().ended()) {
              routingContext.response().setStatusCode(500).setStatusMessage("unexpected server exception: ${error.message}").end()
            }
          }
        }
      }
  }

  protected open fun serialize(value: T): ByteBuffer = value.serializeOnContext().let { ByteBuffer.wrap(it.bytes) }
  protected abstract fun deserialize(data: ByteArray): T

  fun migrate(src: Storage<T>): Future<Unit> {
    val name = this.javaClass.simpleName
    return src.getAll()
      .compose { keyedItems ->
        if (keyedItems.isEmpty()) {
          log.info("$name migration: files are empty; no migration required")
          succeededFuture(Unit)
        } else {
          log.info("$name migrating to mongodb")
          keyedItems.map {
            log.info("$name migrating: $it")
            put(it.key, it.value)
          }.all()
            .compose {
              log.info("$name migration: clearing file-base storage")
              src.clear()
            }
            .onSuccess { log.info("$name migration: done") }
        }
      }
  }
}

