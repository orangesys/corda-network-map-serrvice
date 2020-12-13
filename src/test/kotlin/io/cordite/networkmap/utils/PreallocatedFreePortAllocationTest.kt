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

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class PreallocatedFreePortAllocationTest {
  @Test
  fun `that we can generate a sequence of non-clashing ports`() {
    val assigned = mutableSetOf<Int>()
    val allocator1 = PreallocatedFreePortAllocation(range = 10_001 .. 10_003, assigned = assigned)
    val allocator2 = PreallocatedFreePortAllocation(range = 10_002 .. 10_004, assigned = assigned)
    assertEquals(10_001, allocator1.nextPort())
    assertEquals(10_002, allocator1.nextPort())
    assertEquals(10_003, allocator2.nextPort())
    assertEquals(10_004, allocator2.nextPort())
    assertFails(PreallocatedFreePortAllocation.EOS_MESSAGE) { allocator2.nextPort() }
  }
}