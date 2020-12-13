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
package io.cordite.networkmap.changeset

import io.cordite.networkmap.serialisation.WhitelistSet
import io.cordite.networkmap.serialisation.toWhitelistSet
import io.vertx.core.json.Json
import net.corda.core.crypto.SecureHash
import net.corda.core.node.NetworkParameters
import net.corda.core.node.NotaryInfo
import net.corda.core.node.services.AttachmentId
import net.corda.core.serialization.serialize
import java.time.Instant
import java.util.function.Function

fun changeSet(vararg change: Change): (NetworkParameters) -> NetworkParameters {
  return changeSet(change.asSequence())
}

fun changeSet(change: Collection<Change>): (NetworkParameters) -> NetworkParameters {
  return changeSet(change.asSequence())
}

fun changeSet(changes: Sequence<Change>): (NetworkParameters) -> NetworkParameters {
  return { np: NetworkParameters ->
    changes.fold(np) { acc, change -> change.apply(acc) }.let { it.copy(epoch = it.epoch + 1) }
  }
}

sealed class Change : Function<NetworkParameters, NetworkParameters> {
  val description get() = "${this.javaClass.simpleName!!}: ${Json.encode(this)}"

  data class AddNotary(val notary: NotaryInfo) : Change() {
    override fun apply(networkParameters: NetworkParameters) =
      networkParameters.copy(
        notaries = networkParameters.notaries.toMutableSet().apply { add(notary) }.toList(),
        modifiedTime = Instant.now()
      )
  }

  data class RemoveNotary(val nameHash: SecureHash) : Change() {
    override fun apply(networkParameters: NetworkParameters) =
      networkParameters.copy(
        notaries = networkParameters.notaries.filter { it.identity.name.serialize().hash != nameHash },
        modifiedTime = Instant.now()
      )
  }

  data class ReplaceNotaries(val notaries: List<NotaryInfo>) : Change() {
    override fun apply(networkParameters: NetworkParameters) =
      networkParameters.copy(
        notaries = notaries.distinct(),
        modifiedTime = Instant.now()
      )
  }

  object ClearNotaries : Change() {
    override fun apply(networkParameters: NetworkParameters) =
      networkParameters.copy(
        notaries = emptyList(),
        modifiedTime = Instant.now()
      )
  }

  data class AppendWhiteList(val whitelist: WhitelistSet) : Change() {
    override fun apply(networkParameters: NetworkParameters): NetworkParameters {
      val lhs = networkParameters.whitelistedContractImplementations.toWhitelistSet()
      val amended = lhs + whitelist
      return networkParameters.copy(
        whitelistedContractImplementations = amended.toCordaWhitelist(),
        modifiedTime = Instant.now()
      )
    }
  }

  data class ReplaceWhiteList(val whitelist: WhitelistSet) : Change() {
    override fun apply(networkParameters: NetworkParameters) = networkParameters.copy(
      whitelistedContractImplementations = whitelist.toCordaWhitelist(),
      modifiedTime = Instant.now()
    )
  }

  object ClearWhiteList : Change() {
    override fun apply(t: NetworkParameters) = t.copy(
      whitelistedContractImplementations = emptyMap(),
      modifiedTime = Instant.now()
    )
  }
  
  data class ReplaceAllNetworkParameters(val newNetworkParameters: NetworkParameters) : Change() {
    override fun apply(networkParameters: NetworkParameters) =
        newNetworkParameters.copy(
            modifiedTime = Instant.now()
        )
  }
}