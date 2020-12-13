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

import io.cordite.networkmap.service.NetworkMapService
import io.vertx.core.Future
import io.vertx.core.Future.future
import io.vertx.core.Vertx
import net.corda.core.crypto.sign
import net.corda.core.identity.PartyAndCertificate
import net.corda.core.node.NodeInfo
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.node.services.network.NetworkMapClient
import net.corda.nodeapi.internal.NodeInfoAndSigned
import net.corda.nodeapi.internal.crypto.CertificateType
import net.corda.nodeapi.internal.crypto.X509Utilities
import net.corda.testing.core.ALICE_NAME
import net.corda.testing.node.internal.MOCK_VERSION_INFO
import java.net.URL
import java.security.cert.X509Certificate
import java.time.Duration

class NMSUtil {
	companion object {
		fun waitForNMSUpdate(vertx: Vertx): Future<Long> {
			val extraWait = Duration.ofSeconds(15) // to give a bit more time for CPU starved environments to catchup
			val milliseconds = (NETWORK_PARAM_UPDATE_DELAY + CACHE_TIMEOUT + extraWait).toMillis()
			return future<Long>().apply { vertx.setTimer(milliseconds, this::complete) }
		}
		
		fun createAliceSignedNodeInfo(service: NetworkMapService): NodeInfoAndSigned {
			val cm = service.certificateManager
			// create the certificate chain from the doorman to node CA to legal identity
			val nodeCA = cm.createCertificateAndKeyPair(cm.doormanCertAndKeyPair, ALICE_NAME, CertificateType.NODE_CA)
			val legalIdentity = cm.createCertificateAndKeyPair(nodeCA, ALICE_NAME, CertificateType.LEGAL_IDENTITY)
			val certPath = X509Utilities.buildCertPath(
				legalIdentity.certificate,
				nodeCA.certificate,
				cm.doormanCertAndKeyPair.certificate,
				cm.rootCertificateAndKeyPair.certificate
			)
			val alicePartyAndCertificate = PartyAndCertificate(certPath)
			val ni = NodeInfo(listOf(NetworkHostAndPort("localhost", 10001)), listOf(alicePartyAndCertificate), 1, 1)
			return NodeInfoAndSigned(ni) { _, serialised ->
				legalIdentity.keyPair.private.sign(serialised.bytes)
			}
		}
		
		fun createNetworkMapClient(rootCert: X509Certificate, port: Int): NetworkMapClient {
			return NetworkMapClient(URL("http://localhost:$port$DEFAULT_NETWORK_MAP_ROOT"), MOCK_VERSION_INFO).apply {
				start(rootCert)
			}
		}
	}
}