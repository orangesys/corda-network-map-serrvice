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
import io.cordite.networkmap.utils.NMSUtil.Companion.createNetworkMapClient
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientOptions
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.getOrThrow
import net.corda.core.utilities.loggerFor
import net.corda.testing.node.User
import net.corda.testing.node.internal.SharedCompatibilityZoneParams
import net.corda.testing.node.internal.internalDriver
import org.junit.*
import org.junit.runner.RunWith
import java.net.URL
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@RunWith(VertxUnitRunner::class)
class AllowNodeKeyChangeTest {
	companion object {
		val log = loggerFor<AllowNodeKeyChangeTest>()
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
		vertx = Vertx.vertx()
		val nmsOptions = NMSOptions(
			dbDirectory = dbDirectory,
			user = InMemoryUser(ADMIN_NAME, ADMIN_USER_NAME, ADMIN_PASSWORD),
			port = PORT,
			cacheTimeout = CACHE_TIMEOUT,
			tls = false,
			webRoot = DEFAULT_NETWORK_MAP_ROOT,
			paramUpdateDelay = NETWORK_PARAM_UPDATE_DELAY,
			storageType = StorageType.FILE,
			allowNodeKeyChange = true
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
	fun `that we can register node twice with same legal name but different keys`(context: TestContext) {
		val rootCert = service.certificateManager.rootCertificateAndKeyPair.certificate
		
		val user = User("user1", "test", permissions = setOf("InvokeRpc.getNetworkParameters", "InvokeRpc.networkMapSnapshot"))
		
		val zoneParams = SharedCompatibilityZoneParams(URL("http://localhost:$PORT$DEFAULT_NETWORK_MAP_ROOT"), null, {
			service.addNotaryInfos(it).getOrThrow()
			log.info("notary initialised")
		}, rootCert)
		
		internalDriver(
			portAllocation = PreallocatedFreePortAllocation(),
			compatibilityZone = zoneParams,
			notarySpecs = listOf(),
			notaryCustomOverrides = mapOf("devMode" to false),
			startNodesInProcess = false,
			driverDirectory = createTempDir("tmp1").toPath()
		) {
			log.info("start up the node")
			val node = startNode(
				providedName = CordaX500Name("CordaTestNode", "Southworld", "GB"),
				rpcUsers = listOf(user),
				customOverrides = mapOf("devMode" to false)
			).getOrThrow()
			node.stop()
		}
		internalDriver(
			portAllocation = PreallocatedFreePortAllocation(),
			compatibilityZone = zoneParams,
			notarySpecs = listOf(),
			notaryCustomOverrides = mapOf("devMode" to false),
			startNodesInProcess = false,
			driverDirectory = createTempDir("tmp2").toPath()
		) {
			log.info("start up the node")
			val node = startNode(
				providedName = CordaX500Name("CordaTestNode", "Southworld", "GB"),
				rpcUsers = listOf(user),
				customOverrides = mapOf("devMode" to false)
			).getOrThrow()
			val nodeRpc = node.rpc
			log.info("node started. going to sleep to wait for the NMS to update")
			NMSUtil.waitForNMSUpdate(vertx) // plenty of time for the NMS to synchronise
			val nmc = createNetworkMapClient(rootCert, PORT)
			val nm = nmc.getNetworkMap().payload
			val nodeNodes = nodeRpc.networkMapSnapshot().toSet()
			val nmNodes = nm.nodeInfoHashes.map { nmc.getNodeInfo(it) }.toSet()
			context.assertTrue(nmNodes.size==nodeNodes.size)
			}
	}
	
	@Test
	fun `that we can test publish api for the same node name with a different key`(context: TestContext) {
		val rootCert = service.certificateManager.rootCertificateAndKeyPair.certificate
		val nmc = createNetworkMapClient(rootCert, PORT)
		
		val sni1 = NMSUtil.createAliceSignedNodeInfo(service)
		nmc.publish(sni1.signed)
		//Thread.sleep(NETWORK_MAP_QUEUE_DELAY.toMillis() * 2)
		NMSUtil.waitForNMSUpdate(vertx)
		val nm = nmc.getNetworkMap().payload
		val nhs = nm.nodeInfoHashes
		context.assertEquals(1, nhs.size)
		assertEquals(sni1.signed.raw.hash, nhs[0])
		
		val sni2 = NMSUtil.createAliceSignedNodeInfo(service)
		
		val pk1 = sni1.nodeInfo.legalIdentities.first().owningKey
		val pk2 = sni2.nodeInfo.legalIdentities.first().owningKey
		assertNotEquals(pk1, pk2)
		try {
			nmc.publish(sni2.signed)
			NMSUtil.waitForNMSUpdate(vertx)
			assertEquals(2, nmc.getNetworkMap().payload.nodeInfoHashes.size)
		} catch (err: Throwable) {
				throw err
		}
	}
}