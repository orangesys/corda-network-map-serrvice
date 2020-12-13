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

import io.cordite.networkmap.changeset.Change
import io.cordite.networkmap.changeset.changeSet
import io.cordite.networkmap.serialisation.deserializeOnContext
import io.cordite.networkmap.serialisation.parseWhitelist
import io.cordite.networkmap.service.NetworkMapServiceProcessor
import io.cordite.networkmap.storage.file.NetworkParameterInputsStorage
import io.vertx.core.Future
import io.vertx.core.Vertx
import net.corda.core.node.NotaryInfo
import net.corda.nodeapi.internal.SignedNodeInfo
import org.slf4j.LoggerFactory
import java.io.File
import java.time.Instant

/** the name of this logger is used in [logback-test.xml] */
private val logger = LoggerFactory.getLogger("test-data")


fun NetworkMapServiceProcessor.initialiseWithTestData(vertx: Vertx, includeNodes : Boolean = true) : Future<Unit> {
  logger.info("initialising test data")

  val changes = createValidatingNotaryChanges(vertx) + createNonValidatingNotaryChanges(vertx) + createWhitelistChanges(vertx)

  logger.info("initialising network parameters with test data")
  changes.forEach {
    logger.info(" $it")
  }

  return this.updateNetworkParameters(changeSet(changes), "initialising state", Instant.now())
    .compose {
      when {
        includeNodes -> processNodeInfos(vertx)
        else -> Future.succeededFuture(Unit)
      }
    }
    .onSuccess { logger.info("test data initialised") }
    .catch { logger.error("failed to initialise test data", it) }
}

private fun NetworkMapServiceProcessor.processNodeInfos(vertx: Vertx): Future<Unit>? {
  logger.info("initialising with nodes")
  return collectNodeInfos(vertx)
    .fold(Future.succeededFuture(Unit)) { acc, signedNodeInfo ->
      // we do these ops in sequence - consider running them in parallel
      logger.info(" ${signedNodeInfo.raw.hash}")
      acc.compose { this.addNode(signedNodeInfo) }
    }
}

private fun collectNodeInfos(vertx: Vertx) = File(SAMPLE_NODES).getFiles()
  .map { file -> vertx.fileSystem().readFileBlocking(file.absolutePath).bytes.deserializeOnContext<SignedNodeInfo>() }

private fun createWhitelistChanges(vertx: Vertx) =
  File(SAMPLE_INPUTS, NetworkParameterInputsStorage.WHITELIST_NAME).let { vertx.fileSystem().readFileBlocking(it.absolutePath).toString() }
    .let { Change.ReplaceWhiteList(it.parseWhitelist()) }

private fun createNonValidatingNotaryChanges(vertx: Vertx): Sequence<Change> {
  return File(SAMPLE_INPUTS, NetworkParameterInputsStorage.DEFAULT_DIR_NON_VALIDATING_NOTARIES).getFiles()
    .map { file -> vertx.fileSystem().readFileBlocking(file.absolutePath).bytes.deserializeOnContext<SignedNodeInfo>().verified() }
    .map { Change.AddNotary(NotaryInfo(it.legalIdentities.first(), false)) as Change }
}

private fun createValidatingNotaryChanges(vertx: Vertx): Sequence<Change> {
  return File(SAMPLE_INPUTS, NetworkParameterInputsStorage.DEFAULT_DIR_VALIDATING_NOTARIES).getFiles()
    .map { file -> vertx.fileSystem().readFileBlocking(file.absolutePath).bytes.deserializeOnContext<SignedNodeInfo>().verified() }
    .map { Change.AddNotary(NotaryInfo(it.legalIdentities.first(), true)) as Change }
}