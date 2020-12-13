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

import io.cordite.networkmap.storage.file.NetworkParameterInputsStorage
import io.cordite.networkmap.storage.file.NetworkParameterInputsStorage.Companion.DEFAULT_DIR_NAME
import io.cordite.networkmap.storage.file.NetworkParameterInputsStorage.Companion.DEFAULT_DIR_NON_VALIDATING_NOTARIES
import io.cordite.networkmap.storage.file.NetworkParameterInputsStorage.Companion.DEFAULT_DIR_VALIDATING_NOTARIES
import io.cordite.networkmap.storage.file.NetworkParameterInputsStorage.Companion.WHITELIST_NAME
import io.cordite.networkmap.utils.*
import io.vertx.core.Vertx
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import org.junit.*
import org.junit.runner.RunWith
import java.io.File
import java.nio.file.Files


@RunWith(VertxUnitRunner::class)
class NetworkParameterInputsStorageTest {
  companion object {
    @JvmField
    @ClassRule
    val mdcClassRule = JunitMDCRule()

    private lateinit var vertx: Vertx

    @JvmStatic
    @BeforeClass
    fun before() {
      SerializationTestEnvironment.init()
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
  fun `that we create the input folder`(context: TestContext) {
    val tempDir = createTempDirectory()
    val nmis = NetworkParameterInputsStorage(vertx, tempDir)
    nmis.makeDirs()
      .onSuccess {
        val rootDir = File(tempDir, DEFAULT_DIR_NAME)
        context.assertTrue(rootDir.exists())
        context.assertTrue(File(rootDir, DEFAULT_DIR_VALIDATING_NOTARIES).exists())
        context.assertTrue(File(rootDir, DEFAULT_DIR_NON_VALIDATING_NOTARIES).exists())
      }
      .setHandler(context.asyncAssertSuccess())
  }

  @Test
  fun `that digest stream correctly signals a change in the input set`(context: TestContext) {
    val tempDir = createTempDirectory()
    val nmis = NetworkParameterInputsStorage(vertx, tempDir)

    var initialDigest = ""
    var newDigest = ""

    nmis.makeDirs()
      .onSuccess { println("directories created in ${nmis.directory}") }
      .compose { nmis.digest() }
      .onSuccess {
        initialDigest = it
        println("initial digest: $it")
      }
      .compose {
        val src = File("${SAMPLE_INPUTS}whitelist.txt").absolutePath
        val dst = File(nmis.directory, WHITELIST_NAME).absolutePath
        println("copy $src to $dst")
        vertx.fileSystem().copy(src, dst)
      }
      .compose { nmis.digest() }
      .onSuccess {
        newDigest = it
        context.assertNotEquals(initialDigest, newDigest)
        println("new digest: $it")
      }
      .onSuccess {
        // setup the listener
        val async = context.async(1)
        nmis.registerForChanges().subscribe {
          println("change received: $it")
          context.assertEquals(newDigest, it, "new digest from publication should match the actual digest for the change")
          async.countDown()
        }
      }
      .setHandler(context.asyncAssertSuccess())
  }

  @Test
  fun `that we can load whitelist and notaries`(context: TestContext) {
    val tempDir = createTempDirectory()
    val nmis = NetworkParameterInputsStorage(vertx, tempDir)
    val async = context.async()

    nmis.makeDirs()
      .onSuccess { println("directories created in ${nmis.directory}") }
      .onSuccess {
        // copy the whitelist
        Files.copy("$SAMPLE_INPUTS$WHITELIST_NAME".toPath(), nmis.whitelistPath.toPath())
        copyFolder("$SAMPLE_INPUTS$DEFAULT_DIR_VALIDATING_NOTARIES".toPath(), nmis.validatingNotariesPath.toPath())
        copyFolder("$SAMPLE_INPUTS$DEFAULT_DIR_NON_VALIDATING_NOTARIES".toPath(), nmis.nonValidatingNotariesPath.toPath())
      }
      .onSuccess {
        // setup the listener
        nmis.registerForChanges().subscribe {
          println("change received: $it")
          nmis.readWhiteList()
            .onSuccess {
              context.assertEquals(14, it.size)
            }
            .compose {
              nmis.readNotaries()
            }
            .onSuccess {
              context.assertEquals(1, it.count { it.second.validating })
              context.assertEquals(1, it.count { !it.second.validating })
            }
            .onSuccess { async.complete() }
            .catch {
              context.fail(it)
            }
        }
      }
      .setHandler(context.asyncAssertSuccess())
  }

  private fun createTempDirectory(): File {
    return Files.createTempDirectory("nms-test").toFile().apply { deleteOnExit() }
  }

}