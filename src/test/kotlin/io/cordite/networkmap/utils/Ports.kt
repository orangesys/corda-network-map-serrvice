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

import net.corda.core.utilities.loggerFor
import net.corda.testing.driver.PortAllocation
import java.io.IOException
import java.net.ServerSocket

fun getFreePort(): Int {
  PreallocatedFreePortAllocation.DEFAULT_ALLOCATOR.nextPort()
  return ServerSocket(0).use { it.localPort }
}

class PreallocatedFreePortAllocation(
  private val range: IntRange = 10_000 .. 30_000,
  private val assigned : MutableSet<Int> = DEFAULT_ASSIGNED_SET
) : PortAllocation() {
  companion object {
    private val log = loggerFor<PreallocatedFreePortAllocation>()
    internal const val EOS_MESSAGE = "no unassigned port found"
    private val DEFAULT_ASSIGNED_SET = mutableSetOf<Int>()
    internal val DEFAULT_ALLOCATOR = PreallocatedFreePortAllocation()
  }

  private val fountain = generateSequence(range.first) {
    val next = it + 1
    if (next <= range.last) {
      next
    } else {
      null
    }
  }.filter {
      try {
        ServerSocket(it).use {
          log.debug("found unbound port $it")
          true
        }
      } catch (ex: IOException) {
        log.debug("port $it is bound")
        false
      }
    }
    .iterator()

  override fun nextPort(): Int {
    return try {
      generateSequence { fountain.next() }
        .filter { attemptToAssignPort(it) }
        .first()
    } catch (ex: NoSuchElementException) {
      error(EOS_MESSAGE)
    }.apply {
      log.info("allocating port: ${this}")
    }
  }

  private fun attemptToAssignPort(port: Int) : Boolean {
    synchronized(assigned) {
      return if (assigned.contains(port)) {
        false
      } else {
        assigned.add(port)
        true
      }
    }
  }
}