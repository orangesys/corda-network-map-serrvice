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
package io.cordite.networkmap

import io.bluebank.braid.core.logging.loggerFor
import io.cordite.networkmap.service.NetworkMapService
import io.cordite.networkmap.utils.NMSOptions
import io.cordite.networkmap.utils.NMSOptionsParser
import kotlin.system.exitProcess


open class NetworkMapApp {
  companion object {
    private val logger = loggerFor<NetworkMapApp>()

    @JvmStatic
    fun main(args: Array<String>) {
      NMSOptionsParser().apply {
        if (args.contains("--help")) {
          printHelp()
          return
        }
        println("starting networkmap with the following options")
        printOptions()
        bootstrapNMS()
      }
    }

    private fun NMSOptionsParser.bootstrapNMS() {
      NMSOptions.parse(this).apply {
        if (truststore != null && !truststore.exists()) {
          println("failed to find truststore ${truststore.path}")
          exitProcess(-1)
        }
        NetworkMapService(this).startup().setHandler {
          if (it.failed()) {
            logger.error("failed to complete setup", it.cause())
          } else {
            logger.info("started")
          }
        }
      }
    }
  }
}
