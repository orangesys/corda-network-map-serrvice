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
package io.cordite.networkmap.service

import io.bluebank.braid.core.async.getOrThrow
import io.cordite.networkmap.utils.*
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientOptions
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.getOrThrow
import net.corda.core.utilities.loggerFor
import net.corda.node.services.network.NetworkMapClient
import net.corda.testing.node.NotarySpec
import net.corda.testing.node.User
import net.corda.testing.node.internal.MOCK_VERSION_INFO
import net.corda.testing.node.internal.SharedCompatibilityZoneParams
import net.corda.testing.node.internal.internalDriver
import org.junit.*
import org.junit.runner.RunWith
import java.io.File
import java.net.URL
import java.security.cert.X509Certificate

@RunWith(VertxUnitRunner::class)
class MongoBasedNetworkMapWithRootTest {
	companion object {
		val log = loggerFor<MongoBasedNetworkMapWithRootTest>()
		
		@JvmField
		@ClassRule
		val mdcClassRule = JunitMDCRule()
		
		@JvmStatic
		@BeforeClass
		fun beforeClass() {
			log.info("before class running")
			SerializationTestEnvironment.init()
		}
	}
	
	@JvmField
	@Rule
	val mdcRule = JunitMDCRule()
	
	private val PORT = getFreePort()
	private val dbDirectory = createTempDir()
	private lateinit var vertx: Vertx
	private lateinit var service: NetworkMapService
	private lateinit var client: HttpClient
	
	@Before
	fun before(context: TestContext) {
		log.info("preparing test environment")
		// we'll need to have a serialization context so that the NMS can set itself up
		// BUT we can't use the one used by the application
		val async = context.async()
		
		val rootCAFilePath = File("src/test/resources/certificates/root/keytool-generated-root.jks").absolutePath
		
		vertx = Vertx.vertx()
		val nmsOptions = NMSOptions(
			dbDirectory = dbDirectory,
			user = InMemoryUser(ADMIN_NAME, ADMIN_USER_NAME, ADMIN_PASSWORD),
			port = PORT,
			cacheTimeout = CACHE_TIMEOUT,
			tls = false,
			webRoot = DEFAULT_NETWORK_MAP_ROOT,
			paramUpdateDelay = NETWORK_PARAM_UPDATE_DELAY,
			storageType = StorageType.MONGO,
			rootCAFilePath = rootCAFilePath
		)
		service = NetworkMapService(nmsOptions = nmsOptions, vertx = vertx)
		
		service.startup().setHandler {
			when {
				it.succeeded() -> async.complete()
				else -> context.fail(it.cause())
			}
		}
		
		client = vertx.createHttpClient(HttpClientOptions()
			.setDefaultHost(DEFAULT_HOST)
			.setDefaultPort(PORT)
			.setSsl(false)
			.setTrustAll(true)
			.setVerifyHost(false)
		)
	}
	
	@After
	fun after(context: TestContext) {
		log.info("closing test")
		try {
			val async = context.async()
			service.shutdown()
			vertx.close {
				context.assertTrue(it.succeeded())
				async.complete()
			}
		} catch (err: Throwable) {
			log.error("failed to shutdown cleanly", err)
		}
	}
	
	@Test
	fun `run node, check node count`(context: TestContext) {
		val portAllocation = PreallocatedFreePortAllocation()
		
		val rootCert = service.certificateManager.rootCertificateAndKeyPair.certificate
		
		val zoneParams = SharedCompatibilityZoneParams(URL("http://localhost:$PORT$DEFAULT_NETWORK_MAP_ROOT"), null, {
			service.addNotaryInfos(it).getOrThrow()
			log.info("notary initialised")
		}, rootCert)
		
		internalDriver(
			portAllocation = portAllocation,
			compatibilityZone = zoneParams,
			notarySpecs = listOf(NotarySpec(CordaX500Name("NotaryService", "Zurich", "CH"))),
			notaryCustomOverrides = mapOf("devMode" to false),
			startNodesInProcess = false,
			driverDirectory = createTempDir().toPath()
		) {
			val user = User("user1", "test", permissions = setOf("InvokeRpc.getNetworkParameters", "InvokeRpc.networkMapSnapshot"))
			log.info("start up the node")
			
			val node = startNode(
				providedName = CordaX500Name("CordaTestNode", "Southwold", "GB"),
				rpcUsers = listOf(user),
				customOverrides = mapOf("devMode" to false)
			).getOrThrow()
			
			val nodeRpc = node.rpc
			log.info("node started. going to sleep to wait for the NMS to update")
			NMSUtil.waitForNMSUpdate(vertx) // plenty of time for the NMS to synchronise
			val nmc = createNetworkMapClient(context, rootCert)
			val nm = nmc.getNetworkMap().payload
			val nmp = nmc.getNetworkParameters(nm.networkParameterHash).verified()
			context.assertEquals(nodeRpc.networkParameters, nmp)
			val nodeNodes = nodeRpc.networkMapSnapshot().toSet()
			val nmNodes = nm.nodeInfoHashes.map { nmc.getNodeInfo(it) }.toSet()
			context.assertEquals(nodeNodes, nmNodes)
			context.assertEquals(2, nodeNodes.size)
			log.info("corda network node has the same nodes as the network map")
		}
	}
	private fun createNetworkMapClient(context: TestContext, rootCert: X509Certificate): NetworkMapClient {
		val async = context.async()
		service.storages.certAndKeys.get(CertificateManager.NETWORK_MAP_CERT_KEY)
			.onSuccess {
				context.put<X509Certificate>("cert", it.certificate)
				async.complete()
			}
			.setHandler(context.asyncAssertSuccess())
		async.awaitSuccess()
		return NetworkMapClient(URL("http://localhost:$PORT$DEFAULT_NETWORK_MAP_ROOT"), MOCK_VERSION_INFO).apply {
			start(rootCert)
		}
	}
}