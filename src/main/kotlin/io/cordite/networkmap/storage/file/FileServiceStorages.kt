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
package io.cordite.networkmap.storage.file

import io.bluebank.braid.core.async.mapUnit
import io.cordite.networkmap.service.ServiceStorages
import io.cordite.networkmap.storage.Storage
import io.cordite.networkmap.utils.NMSOptions
import io.cordite.networkmap.utils.all
import io.vertx.core.Future
import io.vertx.core.Vertx
import net.corda.core.crypto.SecureHash
import net.corda.nodeapi.internal.SignedNodeInfo
import net.corda.nodeapi.internal.crypto.CertificateAndKeyPair
import net.corda.nodeapi.internal.network.ParametersUpdate
import net.corda.nodeapi.internal.network.SignedNetworkParameters

class FileServiceStorages(vertx: Vertx, nmsOptions: NMSOptions) : ServiceStorages() {
  override val certAndKeys : Storage<CertificateAndKeyPair> = CertificateAndKeyPairStorage(vertx, nmsOptions.dbDirectory)
  val input = NetworkParameterInputsStorage(vertx, nmsOptions.dbDirectory)
  override val nodeInfo : Storage<SignedNodeInfo> = SignedNodeInfoStorage(vertx, nmsOptions.dbDirectory)
  override val networkParameters : Storage<SignedNetworkParameters> = SignedNetworkParametersStorage(vertx, nmsOptions.dbDirectory)
  override val parameterUpdate : Storage<ParametersUpdate> = ParametersUpdateStorage(vertx, nmsOptions.dbDirectory)
  override val text = TextStorage(vertx, nmsOptions.dbDirectory)
  override val latestAcceptedParameters: Storage<SecureHash> = SecureHashStorage(vertx, nmsOptions.dbDirectory)

  override fun setupStorage(): Future<Unit> {
    return all(
      (certAndKeys as CertificateAndKeyPairStorage).makeDirs(),
      input.makeDirs(),
      (nodeInfo as SignedNodeInfoStorage).makeDirs(),
      (networkParameters as SignedNetworkParametersStorage).makeDirs(),
      (parameterUpdate as ParametersUpdateStorage).makeDirs(),
      text.makeDirs(),
      (latestAcceptedParameters as SecureHashStorage).makeDirs()
    ).mapUnit()
  }
}