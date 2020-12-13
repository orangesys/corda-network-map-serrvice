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

import io.cordite.networkmap.utils.*
import io.cordite.networkmap.utils.NMSUtil.Companion.createNetworkMapClient
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientOptions
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import net.corda.core.utilities.days
import org.junit.*
import org.junit.runner.RunWith
import java.io.File
import kotlin.test.assertEquals

@RunWith(VertxUnitRunner::class)
class NetworkMapWithNMPTest {
	companion object {
		@JvmField
		@ClassRule
		val mdcClassRule = JunitMDCRule()
		
		@JvmStatic
		@BeforeClass
		fun beforeClass() {
			SerializationTestEnvironment.init()
		}
	}
	
	@JvmField
	@Rule
	val mdcRule = JunitMDCRule()
	
	private val PORT = getFreePort()
	private val dbDirectory = createTempDir()
	private var vertx = Vertx.vertx()
	private lateinit var service: NetworkMapService
	private lateinit var client: HttpClient
	
	@Before
	fun before(context: TestContext) {
		vertx = Vertx.vertx()
		val nmpPath = File("src/test/resources/network-parameters/network-parameters.json").absolutePath
		
		client = vertx.createHttpClient(HttpClientOptions()
			.setDefaultHost("127.0.0.1")
			.setDefaultPort(PORT)
			.setSsl(true)
			.setTrustAll(true)
			.setVerifyHost(false)
		)
		
		val nmsOptions = NMSOptions(
			dbDirectory = dbDirectory,
			user = InMemoryUser("", "sa", ""),
			port = PORT,
			cacheTimeout = CACHE_TIMEOUT,
			tls = false,
			webRoot = DEFAULT_NETWORK_MAP_ROOT,
			paramUpdateDelay = NETWORK_PARAM_UPDATE_DELAY,
			enableDoorman = false,
			enableCertman = true,
			pkix = false,
			truststore = null,
			trustStorePassword = null,
			strictEV = false,
			storageType = StorageType.FILE,
			networkParametersPath = nmpPath
		)
		this.service = NetworkMapService(nmsOptions)
		
		val completed = Future.future<Unit>()
		service.startup().setHandler(completed)
		completed
			.compose { service.processor.initialiseWithTestData(vertx) }
			.setHandler(context.asyncAssertSuccess())
	}
	
	@After
	fun after(context: TestContext) {
		client.close()
		service.shutdown()
		val async = context.async()
		vertx.close {
			context.assertTrue(it.succeeded())
			async.complete()
		}
	}
	
	@Test
	fun `that we can configure network map with network parameters`() {
		val rootCert = service.certificateManager.rootCertificateAndKeyPair.certificate
		val nmc = createNetworkMapClient(rootCert, PORT)
		val nmp = nmc.getNetworkParameters(nmc.getNetworkMap().payload.networkParameterHash).verified()
		assertEquals(nmp.minimumPlatformVersion, 4)
		assertEquals(nmp.maxMessageSize, 10485760)
		assertEquals(nmp.maxTransactionSize, Int.MAX_VALUE)
		assertEquals(nmp.epoch, 2)
		assertEquals(nmp.eventHorizon, 30.days)
		assertEquals(nmp.packageOwnership, emptyMap())
	}
}