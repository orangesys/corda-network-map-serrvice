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
package io.cordite.networkmap.serialisation

import com.fasterxml.jackson.annotation.JsonProperty
import net.corda.core.node.AutoAcceptable
import net.corda.core.node.NotaryInfo
import net.corda.core.node.services.AttachmentId
import java.security.PublicKey
import java.time.Duration
import java.time.Instant

class NetworkParametersMixin constructor(
	@JsonProperty("minimumPlatformVersion") val minimumPlatformVersion: Int = 4,
	@JsonProperty("notaries") val notaries: List<NotaryInfo> = listOf(),
	@JsonProperty("maxMessageSize") val maxMessageSize: Int = 10485760,
	@JsonProperty("maxTransactionSize") val maxTransactionSize: Int = Int.MAX_VALUE,
	@JsonProperty("modifiedTime") @AutoAcceptable val modifiedTime: Instant = Instant.now(),
	@JsonProperty("epoch") @AutoAcceptable val epoch: Int = 1,
	@JsonProperty("whitelistedContractImplementations") @AutoAcceptable val whitelistedContractImplementations: Map<String, List<AttachmentId>> = mapOf(),
	@JsonProperty("eventHorizon") val eventHorizon: Long = Duration.ofDays(30).seconds,
	@JsonProperty("packageOwnership") val packageOwnership: Map<String, PublicKey> = emptyMap()
)