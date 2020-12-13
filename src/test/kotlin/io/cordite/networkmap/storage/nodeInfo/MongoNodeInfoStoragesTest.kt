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
package io.cordite.networkmap.storage.nodeInfo

import com.fasterxml.jackson.core.type.TypeReference
import io.cordite.networkmap.service.*
import io.cordite.networkmap.utils.*
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.json.Json
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import org.junit.*
import org.junit.runner.RunWith
import java.io.File
import java.time.Duration

@RunWith(VertxUnitRunner::class)
class MongoNodeInfoStoragesTest {
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
	private lateinit var vertx: Vertx
	private lateinit var service: NetworkMapService
	private lateinit var client: HttpClient
	
	@Before
	fun before(context: TestContext) {
		vertx = Vertx.vertx()
		val async = context.async()
		val nmsOptions = NMSOptions(
			dbDirectory = dbDirectory,
			user = InMemoryUser(ADMIN_NAME, ADMIN_USER_NAME, ADMIN_PASSWORD),
			port = PORT,
			cacheTimeout = CACHE_TIMEOUT,
			tls = false,
			webRoot = DEFAULT_NETWORK_MAP_ROOT,
			paramUpdateDelay = NETWORK_PARAM_UPDATE_DELAY,
			storageType = StorageType.FILE
		)
		
		this.service = NetworkMapService(nmsOptions)
		
		this.service.startup().setHandler {
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
		
		async.await()
		
		service.processor.initialiseWithTestData(vertx).setHandler(context.asyncAssertSuccess())
		
	}
	
	@After
	fun after(context: TestContext) {
		service.shutdown()
		val async = context.async()
		vertx.close {
			context.assertTrue(it.succeeded())
			async.complete()
		}
	}
	
	@Test
	fun `that we can get node keys size`(context: TestContext) {
		var size = 0
		service.storages.nodeInfo.getKeys().onSuccess {
			size = it.size
		}
		service.storages.nodeInfo.size().onSuccess {
			context.assertEquals(size, it)
		}
	}
	
	@Test
	fun `that we can get nodes based on page number`(context: TestContext) {
		val async = context.async()
		client.futureGet("$DEFAULT_NETWORK_MAP_ROOT$ADMIN_REST_ROOT/nodes/paging-summary?pageSize=5")
			.onSuccess {
				val nodeInfoPagingSummary = Json.decodeValue(it, object : TypeReference<NodeInfoPagingSummary>() {})
				context.assertEquals(5, nodeInfoPagingSummary.pageSize, "expected and actual page size are not equal")
				context.assertEquals(2, nodeInfoPagingSummary.totalNoOfNodeInfos, "expected and actual number of nodes are not equal")
			}
			.compose {
				client.futureGet("$DEFAULT_NETWORK_MAP_ROOT$ADMIN_REST_ROOT/nodes/page?pageSize=1&page=1")
			}
			.onSuccess {
				val nodes = Json.decodeValue(it, object : TypeReference<NodeInfosByPage>() {})
				context.assertEquals(1, nodes.simpleNodeInfos.size, "nodes should be correct count")
			}
			.compose {
				client.futureGet("$DEFAULT_NETWORK_MAP_ROOT$ADMIN_REST_ROOT/nodes/page?pageSize=1&page=2")
			}
			.onSuccess {
				val nodes = Json.decodeValue(it, object : TypeReference<NodeInfosByPage>() {})
				context.assertEquals(1, nodes.simpleNodeInfos.size, "nodes should be correct count")
			}
			.onSuccess {
				async.complete()
			}
			.catch(context::fail)
	}
}
