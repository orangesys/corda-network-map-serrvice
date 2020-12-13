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

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class OptionsTest {
  companion object {
    const val STRING_NAME = "a-string"
    const val STRING_VALUE = "string value"
    const val INT_NAME = "an-int"
    const val INT_VALUE = "2"
    const val BOOL_NAME = "a-bool"
    const val BOOL_VALUE = "true"
    const val UNSPECIFIED_STRING_NAME = "no-string"
  }

  @Before
  fun before() {
    listOf(STRING_NAME to STRING_VALUE, INT_NAME to INT_VALUE, BOOL_NAME to BOOL_VALUE).forEach {
      System.getProperties()[it.first] = it.second
    }
  }

  @Test
  fun testOptions() {
    val options = Options()
    assertEquals("default", options.addOption(UNSPECIFIED_STRING_NAME, "default").stringValue)
    assertEquals(STRING_VALUE, options.addOption(STRING_NAME, "default").stringValue)
    assertEquals(INT_VALUE, options.addOption(INT_NAME, "0").stringValue)
    assertEquals(BOOL_VALUE, options.addOption(BOOL_NAME, "false").stringValue)
    // check that we can print help and options with no exceptions
    options.printHelp()
    options.printOptions()
  }
}