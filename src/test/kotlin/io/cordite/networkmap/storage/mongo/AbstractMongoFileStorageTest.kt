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
package io.cordite.networkmap.storage.mongo

import com.mongodb.reactivestreams.client.MongoClient
import io.cordite.networkmap.serialisation.deserializeOnContext
import io.cordite.networkmap.utils.*
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import io.vertx.ext.web.Router
import net.corda.core.serialization.CordaSerializable
import org.junit.*
import org.junit.runner.RunWith
import java.time.Duration

@RunWith(VertxUnitRunner::class)
class AbstractMongoFileStorageTest {
  companion object {

    @JvmField
    @ClassRule
    val mdcClassRule = JunitMDCRule()

    private lateinit var mongoClient: MongoClient

    @JvmStatic
    @BeforeClass
    fun beforeClass() {
      SerializationTestEnvironment.init()
      mongoClient = TestDatabase.createMongoClient()
    }

    @JvmStatic
    @AfterClass
    fun afterClass() {
      mongoClient.close()
    }
  }

  @JvmField
  @Rule
  val mdcRule = JunitMDCRule()

  @CordaSerializable
  data class TestData(val name: String)

  class TestDataStorage(client: MongoClient, dbName: String, bucketName: String) : AbstractMongoFileStorage<TestData>(client, dbName, bucketName) {
    override fun deserialize(data: ByteArray): TestData {
      return data.deserializeOnContext()
    }
  }

  private val vertx = Vertx.vertx()
  private val storage = TestDataStorage(mongoClient, TestDatabase.createUniqueDBName(), "test-data")
  private val port = getFreePort()
  private val fileName = "foo"

  @Before
  fun before(context: TestContext) {
    val async = context.async()
    Router.router(vertx).apply {
      get("/:fileName").handler {
        storage.serve(it.request().getParam("fileName"), it, Duration.ZERO)
      }
      vertx.createHttpServer(HttpServerOptions().setHost("localhost"))
        .requestHandler(this)
        .listen(port) {
          async.complete()
        }
    }
  }

  @After
  fun after(context: TestContext) {
    val async = context.async()
    vertx.close {
      async.complete()
    }
  }

  @Test
  fun `populate storage and retrieve`(context: TestContext) {
    val testName = "vurt feather"
    val async = context.async()
    storage.put(fileName, TestData(testName))
      .compose { storage.get(fileName) }
      .onSuccess { data ->
        context.assertNotNull(data)
        context.assertEquals(testName, data.name)
      }
      .compose { storage.exists(fileName) }
      .onSuccess { exists -> context.assertTrue(exists, "that file exists") }
      .compose { storage.getKeys() }
      .onSuccess {
        context.assertEquals(1, it.size)
        context.assertEquals(fileName, it.first())
      }
      .compose { retrieveTestData(fileName) }
      .onSuccess { data ->
        context.assertNotNull(data)
        context.assertEquals(testName, data.name)
      }
      .compose { storage.delete(fileName) }
      .compose { storage.exists(fileName) }
      .onSuccess { exists -> context.assertFalse(exists, "that file has been really deleted") }
      .onSuccess { async.complete() }
      .catch { context.fail(it) }
  }

  @Test
  fun `that retrieving a file that does not exist returns a 404`(context: TestContext) {
    val async = context.async()
    retrieveTestData("unknown")
      .onSuccess {
        context.fail("expected a failure with message 404 but instead got data!")
      }
      .catch {
        when {
          it.message == "404" -> async.complete()
          else -> context.fail("expected 404 but ${it.message}")
        }
      }
  }

  private fun retrieveTestData(fileName: String): Future<TestData> {
    val client = vertx.createHttpClient(HttpClientOptions().setDefaultPort(port).setDefaultHost("localhost"))
    val result = Future.future<TestData>()
    try {
      @Suppress("DEPRECATION")
      client.get("/$fileName")
      {
        when {
          it.statusCode() != 200 -> result.fail("${it.statusCode()}")
          else -> it.bodyHandler { buffer ->
            try {
              val value = buffer.bytes.deserializeOnContext<TestData>()
              result.complete(value)
            } catch (err: Throwable) {
              result.fail(err)
            } finally {
              client.close()
            }
          }
        }
      }
        .exceptionHandler { err ->
          result.fail(err)
          client.close()
        }
        .end()
    } catch (err: Throwable) {
      result.fail(err)
      client.close()
    }
    return result
  }
  
  @Test
  fun `populate storage and test paging`(context: TestContext) {
    val testName1  = "test1"
    val testName2  = "test2"
    val testName3  = "test3"
    val fileName1  = "file1"
    val fileName2  = "file2"
    val fileName3  = "file3"
    val async = context.async()
    storage.put(fileName1, TestData(testName1))
      .compose { storage.get(fileName1) }
      .onSuccess { data ->
        context.assertNotNull(data)
        context.assertEquals(testName1, data.name)
      }
      .compose { storage.put(fileName2, TestData(testName2)) }
      .compose { storage.get(fileName2) }
      .onSuccess { data ->
        context.assertNotNull(data)
        context.assertEquals(testName2, data.name)
      }
      .compose { storage.put(fileName3, TestData(testName3)) }
      .compose { storage.get(fileName3) }
      .onSuccess { data ->
        context.assertNotNull(data)
        context.assertEquals(testName3, data.name)
      }
      .compose { storage.getPage(1, 2) }
      .onSuccess { data ->
        context.assertNotNull(data)
        context.assertTrue(data.containsKey(fileName1))
        context.assertTrue(data.containsKey(fileName2))
      }
      .compose { storage.getPage(2, 2) }
      .onSuccess { data ->
        context.assertNotNull(data)
        context.assertTrue(data.containsKey(fileName3))
      }
      .onSuccess { async.complete() }
      .catch { context.fail(it) }
  }
}