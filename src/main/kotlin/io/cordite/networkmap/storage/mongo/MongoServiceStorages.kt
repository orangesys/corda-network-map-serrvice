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

import io.bluebank.braid.core.async.mapUnit
import io.cordite.networkmap.service.ServiceStorages
import io.cordite.networkmap.service.StorageType
import io.cordite.networkmap.storage.Storage
import io.cordite.networkmap.storage.file.TextStorage
import io.cordite.networkmap.utils.NMSOptions
import io.cordite.networkmap.utils.all
import io.vertx.core.Future
import io.vertx.core.Vertx
import net.corda.core.crypto.SecureHash

class MongoServiceStorages(private val vertx: Vertx, private val nmsOptions: NMSOptions) : ServiceStorages() {
  init {
    assert(nmsOptions.storageType == StorageType.MONGO) { "mongo service cannot be initiated with storage type set to ${nmsOptions.storageType}"}
  }

  private val mongoClient = MongoStorage.connect(nmsOptions)
  override val certAndKeys = CertificateAndKeyPairStorage(mongoClient, nmsOptions.mongodDatabase)
  override val nodeInfo = SignedNodeInfoStorage(mongoClient, nmsOptions.mongodDatabase)
  override val networkParameters = SignedNetworkParametersStorage(mongoClient, nmsOptions.mongodDatabase)
  override val parameterUpdate  = ParametersUpdateStorage(mongoClient, nmsOptions.mongodDatabase)
  override val text  = MongoTextStorage(mongoClient, nmsOptions.mongodDatabase)
  override val latestAcceptedParameters = SecureHashStorage(mongoClient, nmsOptions.mongodDatabase)

  override fun setupStorage(): Future<Unit> {
    return all(
      networkParameters.migrate(io.cordite.networkmap.storage.file.SignedNetworkParametersStorage(vertx, nmsOptions.dbDirectory)),
      parameterUpdate.migrate(io.cordite.networkmap.storage.file.ParametersUpdateStorage(vertx, nmsOptions.dbDirectory)),
      // TODO: add something to clear down cached networkmaps on the filesystem from previous versions
      text.migrate(TextStorage(vertx, nmsOptions.dbDirectory)),
      nodeInfo.migrate(io.cordite.networkmap.storage.file.SignedNodeInfoStorage(vertx, nmsOptions.dbDirectory)),
      certAndKeys.migrate(io.cordite.networkmap.storage.file.CertificateAndKeyPairStorage(vertx, nmsOptions.dbDirectory)),
      latestAcceptedParameters.migrate(io.cordite.networkmap.storage.file.SecureHashStorage(vertx, nmsOptions.dbDirectory))
    ).mapUnit()
  }
}