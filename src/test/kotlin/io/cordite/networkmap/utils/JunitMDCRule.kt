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

import io.bluebank.braid.core.logging.LogInitialiser
import net.corda.core.utilities.loggerFor
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.slf4j.MDC

class JunitMDCRule : TestWatcher() {
  companion object {
    private val log = loggerFor<JunitMDCRule>()
    const val MDC_CLASS = "test-class"
    const val MDC_NAME = "test-name"
  }

  init {
    LogInitialiser.init()
  }

  override fun starting(description: Description?) {
    description?.apply {
      log.info("starting test: $className $methodName")
      MDC.put(MDC_CLASS, this.className)
      MDC.put(MDC_NAME, this.methodName)
    }
  }

  override fun finished(description: Description?) {
    description?.apply {
      MDC.remove(MDC_CLASS)
      MDC.remove(MDC_NAME)
      log.info("stopping test: $className $methodName")
    }
  }
}