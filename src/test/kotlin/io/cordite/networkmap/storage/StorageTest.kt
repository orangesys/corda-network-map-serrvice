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

import com.google.common.io.Files
import io.cordite.networkmap.storage.file.TextStorage
import io.cordite.networkmap.utils.JunitMDCRule
import io.cordite.networkmap.utils.all
import io.cordite.networkmap.utils.onSuccess
import io.vertx.core.Vertx
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import org.junit.*
import org.junit.runner.RunWith
import java.io.File

@RunWith(VertxUnitRunner::class)
class StorageTest {
  companion object {
    @JvmField
    @ClassRule
    val mdcClassRule = JunitMDCRule()

    private lateinit var vertx: Vertx

    @JvmStatic
    @BeforeClass
    fun before() {
      vertx = Vertx.vertx()
    }

    @JvmStatic
    @AfterClass
    fun after(context: TestContext) {
      vertx.close(context.asyncAssertSuccess())
    }
  }

  @JvmField
  @Rule
  val mdcRule = JunitMDCRule()

  @Test
  fun `that storage creates parent directory`(context: TestContext) {
    val dbDir = createStorageParentDir("db")
    val textStorage = TextStorage(vertx, dbDir)
    textStorage.makeDirs()
      .onSuccess {
        context.assertTrue(dbDir.exists())
        context.assertTrue(File(dbDir, TextStorage.DEFAULT_CHILD_DIR).exists())
      }
      .setHandler(context.asyncAssertSuccess())
  }

  @Test
  fun `that we can store a text value against a key and recover it`(context: TestContext) {
    val dbDir = createStorageParentDir("db")
    val textStorage = TextStorage(vertx, dbDir)
    val key = "foo"
    val value = "bar"
    textStorage.makeDirs()
      .compose { textStorage.put(key, value) }
      .compose { textStorage.getKeys() }
      .onSuccess {
        context.assertEquals(1, it.size)
        context.assertEquals(key, it.first())
      }
      .compose { textStorage.get(key) }
      .onSuccess {
        context.assertEquals(value, it)
      }
      .compose { textStorage.getAll() }
      .onSuccess {
        context.assertEquals(1, it.size)
        context.assertEquals(key, it.keys.first())
        context.assertEquals(value, it[key])
      }
      .setHandler(context.asyncAssertSuccess())
  }


  @Test
  fun `that we can remove a text value`(context: TestContext) {
    val dbDir = createStorageParentDir("db")
    val textStorage = TextStorage(vertx, dbDir)
    val key1 = "foo"
    val key2 = "bar"
    textStorage.makeDirs()
      .compose { textStorage.put(key1, "hello") }
      .compose { textStorage.put(key2, "world") }
      .compose { textStorage.getKeys() }
      .onSuccess {
        context.assertEquals(2, it.size)
        context.assertTrue(it.contains(key1))
        context.assertTrue(it.contains(key2))
      }
      .compose { textStorage.delete(key1) }
      .compose { textStorage.getKeys() }
      .onSuccess {
        context.assertEquals(1, it.size)
        context.assertFalse(it.contains(key1))
        context.assertTrue(it.contains(key2))
      }
      .setHandler(context.asyncAssertSuccess())
  }


  @Test
  fun `that we can clear all entries`(context: TestContext) {
    val dbDir = createStorageParentDir("db")
    val textStorage = TextStorage(vertx, dbDir)
    val count = 100
    textStorage.makeDirs()
      .compose {
        (1..count).map {
          textStorage.put("key-$it", "val")
        }.all()
      }
      .compose { textStorage.getKeys() }
      .onSuccess {
        context.assertEquals(count, it.size)
      }
      .compose { textStorage.clear() }
      .compose { textStorage.getKeys() }
      .onSuccess {
        context.assertEquals(0, it.size)
      }
      .setHandler(context.asyncAssertSuccess())
  }


  private fun createStorageParentDir(storageDirectory: String): File {
    val tempDir = Files.createTempDir()
    tempDir.deleteOnExit()
    return File(tempDir, storageDirectory)
  }
}
