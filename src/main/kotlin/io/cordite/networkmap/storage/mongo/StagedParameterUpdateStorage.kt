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

import com.mongodb.reactivestreams.client.MongoClient
import io.cordite.networkmap.storage.mongo.IndexType.HASHED
import net.corda.core.node.NotaryInfo
import net.corda.core.node.services.AttachmentId

class StagedParameterUpdateStorage(
  private val client: MongoClient,
  private val dbName: String,
  private val collectionName: String = DEFAULT_COLLECTION
) {
  companion object {
    val DEFAULT_COLLECTION = "staged-parameter-update"
  }

  private val collection = client.getDatabase(dbName).getCollection(collectionName)

  init {
    collection.createIndex(HASHED idx StagedNetworkParameterUpdate::name)
  }
}

data class StagedNetworkParameterUpdate(val name: String,
                                        val notaries: List<NotaryInfo>,
                                        val whitelist: Map<String, List<AttachmentId>>)