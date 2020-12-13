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

import java.util.*

class NMSProperties(val properties: Map<String, String> = emptyMap()) : Map<String, String> by properties {
  companion object {
    fun acquireProperties() : NMSProperties {
      return ClassLoader.getSystemClassLoader().getResourceAsStream("nms.build.properties") ?.let { stream ->
        val properties = Properties().apply { this.load(stream) }
        val map = properties.stringPropertyNames().map { name -> name to properties.getProperty(name)!! }.toMap()
        NMSProperties(map)
      } ?: NMSProperties()
    }
  }

  val mavenVersion : String by lazy {
    this["nms.version"] ?: "unspecified"
  }

  val scmVersion : String by lazy {
    this["buildNumber"] ?: "unspecified"
  }

  override fun toString(): String {
    val maxLength = this.keys.fold(0) { acc, name -> Math.max(acc, name.length)}
    return this.map { (key, value) ->
      "${key.padEnd(maxLength)}: $value"
    }.sorted().joinToString("\n")
  }
}