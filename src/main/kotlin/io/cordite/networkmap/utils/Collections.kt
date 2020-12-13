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

fun <T> Sequence<T>.toEnumeration() = asIterable().toEnumeration()
fun <T> Iterable<T>.toEnumeration(): Enumeration<T> {
  val iterator = this.iterator()
  return object : Enumeration<T> {
    override fun hasMoreElements(): Boolean {
      return iterator.hasNext()
    }

    override fun nextElement(): T {
      return iterator.next()
    }
  }
}