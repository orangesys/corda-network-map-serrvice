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


open class Options {
  data class Option(val name: String, val default: String, val description: String = "") {
    val environmentVariable: String = "NMS_" + name.toUpperCase().replace('-', '_')
    fun width() = name.length

    val stringValue by lazy {
      (System.getenv(environmentVariable) ?: System.getProperty(name) ?: default)
    }

    val booleanValue by lazy { stringValue.toBoolean() }
    val intValue by lazy { stringValue.toInt() }
  }

  private val options = mutableListOf<Option>()

  fun addOption(name: String, default: String, description: String = ""): Option {
    val option = Option(name, default, description)
    options.add(option)
    return option
  }

  fun printHelp() {
    val propertyWidth = (options.map { it.width() }.max() ?: 0)
    val envWidth = propertyWidth + 8
    val defaultWidth = (options.map { it.default.length }.max() ?: 0) + 4
    val descriptionWidth = (options.map { it.description.length }.max() ?: 0) + 4

    println("\njava properties (pass with -D<propertyname>=<property-value>) and env variables\n")
    println("| Property".padEnd(propertyWidth + 2) + " | Env Variable".padEnd(envWidth + 3) + " | Default".padEnd(defaultWidth + 3) + " | Description".padEnd(descriptionWidth + 3) + " |")
    println("| --------".padEnd(propertyWidth + 2, '-') + " | ------------".padEnd(envWidth + 3, '-') + " | -------".padEnd(defaultWidth + 3, '-') + " | -----------".padEnd(descriptionWidth + 3, '-') + " |")
    options.toList().sortedBy { it.name }.forEach {
      println("| ${it.name.padEnd(propertyWidth)} | ${it.environmentVariable.padEnd(envWidth)} | ${it.default.padEnd(defaultWidth)} | ${it.description.padEnd(descriptionWidth)} |")
    }
    println()
  }

  fun printOptions() {
    val propertyWidth = (options.map { it.width() }.max() ?: 0)

    options.toList().sortedBy { it.name }.map { it.name to it.stringValue }
      .map { (key, value) ->
        key to if (key.toLowerCase().contains("password")) {
          if (value.isEmpty()) {
            ""
          } else {
            "*".repeat(value.length - 1)
          }
        } else {
          value
        }
      }
      .forEach { (key, value) ->
        println("${key.padEnd(propertyWidth)} - $value")
      }
  }
}