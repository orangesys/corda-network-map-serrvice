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
package io.cordite.networkmap.storage

import com.mongodb.reactivestreams.client.MongoClient
import io.cordite.networkmap.storage.mongo.MongoTextStorage
import io.cordite.networkmap.utils.JunitMDCRule
import io.cordite.networkmap.utils.TestDatabase
import io.cordite.networkmap.utils.catch
import io.cordite.networkmap.utils.onSuccess
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import org.junit.*
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(VertxUnitRunner::class)
class MongoTextStorageTest {
  companion object {
    
    private lateinit var mongoClient: MongoClient
    
    @JvmField
    @ClassRule
    val mdcClassRule = JunitMDCRule()
    
    @JvmStatic
    @BeforeClass
    fun beforeClass() {
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
  
  @Test
  fun testStorage(context: TestContext) {
    val async = context.async()
    val key = "hello"
    val value = "world"
    MongoTextStorage(mongoClient, TestDatabase.createUniqueDBName()).apply {
      this.get("hello")
        .recover { this.put(key, value).map { value } }
        .compose {
          this.get("hello")
        }
        .onSuccess {
          context.assertEquals("world", it)
        }
        .compose {
          this.size()
        }
        .onSuccess {
          context.assertEquals(1, it)
        }
        .onSuccess { async.complete() }
        .catch { context.fail(it) }
    }
  }
  
  @Test
  fun testStorageKeys(context: TestContext) {
    val async = context.async()
    val key = "hello"
    val value = "world"
    MongoTextStorage(mongoClient, TestDatabase.createUniqueDBName()).apply {
      this.get("hello")
        .recover { this.put(key, value).map { value } }
        .compose {
          this.get("hello")
        }
        .onSuccess {
          context.assertEquals("world", it)
        }
        .compose {
          this.put("hello1", "world1")
        }
        .compose {
          this.getKeys()
        }
        .onSuccess {
          assertEquals(2, it.size)
        }
        .onSuccess { async.complete() }
        .catch { context.fail(it) }
    }
  }
  
  @Test
  fun testStoragePaging(context: TestContext) {
    val async = context.async()
    var count = 1
    MongoTextStorage(mongoClient, TestDatabase.createUniqueDBName()).apply {
      this.put("hello$count", "world$count")
        .onSuccess {
          count++
        }
        .compose {
          this.put("hello$count", "world$count")
        }
        .onSuccess {
          count++
        }
        .compose {
          this.put("hello$count", "world$count")
        }
        .onSuccess {
          count++
        }
        .compose {
          this.put("hello$count", "world$count")
        }
        .onSuccess {
          count++
        }
        .compose {
          this.get("hello3")
        }
        .onSuccess {
          context.assertEquals("world3", it)
        }
        .compose {
          this.size()
        }
        .onSuccess {
          context.assertEquals(4, it)
        }
        .compose{
          this.getPage(1, 2)
        }
        .onSuccess {
          context.assertEquals(2, it.size)
          context.assertTrue(it.containsKey("hello1"))
          context.assertTrue(it.containsKey("hello2"))
        }
        .compose{
          this.getPage(2, 2)
        }
        .onSuccess {
          context.assertEquals(2, it.size)
          context.assertTrue(it.containsKey("hello3"))
          context.assertTrue(it.containsKey("hello4"))
        }
        .onSuccess { async.complete() }
        .catch { context.fail(it) }
    }
  }
  
  @Test
  fun testStorageKeysFilter(context: TestContext) {
    val async = context.async()
    val key = "hello"
    val value = "world"
    MongoTextStorage(mongoClient, TestDatabase.createUniqueDBName()).apply {
      this.get("hello")
        .recover { this.put(key, value).map { value } }
        .compose {
          this.get("hello")
        }
        .onSuccess {
          context.assertEquals("world", it)
        }
        .compose {
          this.put("hello1", "world1")
        }
        .compose {
          this.get("hello1")
        }
        .onSuccess {
          context.assertEquals("world1", it)
        }
        .compose {
          this.put("hello2", "world2")
        }
        .compose {
          this.get("hello2")
        }
        .onSuccess {
          context.assertEquals("world2", it)
        }
        .compose {
          this.getKeys()
        }
        .compose {
          assertEquals(3, it.size)
          val filteredKeys = it.drop(1)
          this.getAll(filteredKeys)
        }
        .onSuccess {
          assertEquals(2, it.size)
        }
        .onSuccess { async.complete() }
        .catch { context.fail(it) }
    }
  }
}