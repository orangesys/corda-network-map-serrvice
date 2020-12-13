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

import io.cordite.networkmap.serialisation.WhitelistSet
import io.cordite.networkmap.serialisation.deserializeOnContext
import io.cordite.networkmap.serialisation.parseWhitelist
import io.cordite.networkmap.utils.*
import io.vertx.core.Future
import io.vertx.core.Future.succeededFuture
import io.vertx.core.Vertx
import net.corda.core.identity.Party
import net.corda.core.node.NodeInfo
import net.corda.core.node.NotaryInfo
import net.corda.core.utilities.loggerFor
import net.corda.nodeapi.internal.SignedNodeInfo
import rx.Observable
import rx.subjects.PublishSubject
import java.io.File


/**
 * @Deprecated("this is only provided for migration from earlier versions of network parameters to the database implementation")
 */
class NetworkParameterInputsStorage(private val vertx: Vertx,
                                    parentDir: File,
                                    childDir: String = DEFAULT_DIR_NAME,
                                    validatingNotariesDirectoryName: String = DEFAULT_DIR_VALIDATING_NOTARIES,
                                    nonValidatingNotariesDirectoryName: String = DEFAULT_DIR_NON_VALIDATING_NOTARIES,
                                    pollRate: Long = DEFAULT_WATCH_DELAY) {
  companion object {
    internal val log = loggerFor<NetworkParameterInputsStorage>()
    const val WHITELIST_NAME = "whitelist.txt"
    const val DEFAULT_WATCH_DELAY = 2_000L
    const val DEFAULT_DIR_NAME = "inputs"
    const val DEFAULT_DIR_VALIDATING_NOTARIES = "validating-notaries"
    const val DEFAULT_DIR_NON_VALIDATING_NOTARIES = "non-validating-notaries"
  }

  internal val directory = File(parentDir, childDir)
  internal val whitelistPath = File(directory, WHITELIST_NAME)
  internal val validatingNotariesPath = File(directory, validatingNotariesDirectoryName)
  internal val nonValidatingNotariesPath = File(directory, nonValidatingNotariesDirectoryName)

  private val digest = DirectoryDigest(directory)
  private var lastDigest: String = ""
  private val publishSubject = PublishSubject.create<String>()

  init {
    digest.digest(vertx).setHandler {
      if (it.failed()) {
        log.error("failed to get digest for input director", it.cause())
      } else {
        lastDigest = it.result()
        // setup the watch
        vertx.periodicStream(pollRate).handler {
          digest()
            .onSuccess {
              if (lastDigest != it) {
                lastDigest = it
                publishSubject.onNext(it)
              }
            }
        }
      }
    }
  }

  fun makeDirs(): Future<Unit> {
    return vertx.fileSystem().mkdirs(validatingNotariesPath.absolutePath)
      .compose { vertx.fileSystem().mkdirs(nonValidatingNotariesPath.absolutePath) }
      .map { Unit }
  }

  fun digest(): Future<String> {
    return digest.digest(vertx)
  }

  fun registerForChanges(): Observable<String> {
    return publishSubject
  }

  fun readWhiteList(): Future<WhitelistSet> {
    return try {
      vertx.fileSystem().readFile(whitelistPath.absolutePath)
        .map {
          it.toString().parseWhitelist()
        }
        .onSuccess {
          log.info("retrieved whitelist")
        }
        .recover {
          log.warn("whitelist file not found at ${whitelistPath.absolutePath}")
          succeededFuture(WhitelistSet())
        }
    } catch (err: Throwable) {
      Future.failedFuture(err)
    }
  }


  fun readNotaries(): Future<List<Pair<String, NotaryInfo>>> {
    val validating = readNodeInfos(validatingNotariesPath)
      .compose { nodeInfos ->
        vertx.executeBlocking {
          nodeInfos.mapNotNull { nodeInfo ->
            try {
              nodeInfo.first to NotaryInfo(nodeInfo.second.verified().notaryIdentity(), true)
            } catch (err: Throwable) {
              log.error("failed to process notary", err)
              null
            }
          }
        }
      }

    val nonValidating = readNodeInfos(nonValidatingNotariesPath)
      .compose { nodeInfos ->
        vertx.executeBlocking {
          nodeInfos.mapNotNull { nodeInfo ->
            try {
              nodeInfo.first to NotaryInfo(nodeInfo.second.verified().notaryIdentity(), false)
            } catch (err: Throwable) {
              log.error("failed to process notary", err)
              null
            }
          }
        }
      }

    return all(validating, nonValidating)
      .map { (validating, nonValidating) ->
        val ms = validating.toMutableSet()
        ms.addAll(nonValidating)
        ms.toList()
      }
      .onSuccess {
        log.info("retrieved notaries")
      }
      .catch {
        log.error("failed to retrieve notaries", it)
      }
  }

  private fun readNodeInfos(dir: File): Future<List<Pair<String, SignedNodeInfo>>> {
    return vertx.fileSystem().readFiles(dir.absolutePath)
      .compose { buffers ->
        vertx.executeBlocking {
          buffers.mapNotNull { (file, buffer) ->
            try {
              file to buffer.bytes.deserializeOnContext<SignedNodeInfo>()
            } catch (err: Throwable) {
              log.error("failed to deserialize SignedNodeInfo $file")
              null
            }
          }
        }
      }
  }

  private fun NodeInfo.notaryIdentity(): Party {
    return when (legalIdentities.size) {
      // Single node notaries have just one identity like all other nodes. This identity is the notary identity
      1 -> legalIdentities[0]
      // Nodes which are part of a distributed notary have a second identity which is the composite identity of the
      // cluster and is shared by all the other members. This is the notary identity.
      2 -> legalIdentities[1]
      else -> throw IllegalArgumentException("Not sure how to get the notary identity in this scenerio: $this")
    }
  }

}
