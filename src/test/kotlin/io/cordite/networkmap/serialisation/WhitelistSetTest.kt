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
package io.cordite.networkmap.serialisation

import io.cordite.networkmap.utils.JunitMDCRule
import net.corda.core.node.services.AttachmentId
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WhitelistSetTest {
  companion object {
    const val iouContract = "com.example.contract.IOUContract"
    const val tradeContract = "com.example.contract.TradeContract"
    val hash1 = AttachmentId.parse("11027FEAAD6C9696122ED46FB9134B4C34D6946E039476E982D6B81C17158F83")
    val hash2 = AttachmentId.parse("A0FF6D5BD46520F346468A74E7D8C58F51D353D4F38F0E34DFE920D9991FAAFF")
    val hash3 = AttachmentId.parse("A0FF6D5BD46520F346468A74E7D8C58F51D353D4F38F0E34DFE920D9991FAAFE")
  }

  @JvmField
  @Rule
  val mdcRule = JunitMDCRule()

  @Test
  fun `that we can parse a white list with multiple hashes per contract`() {

    // text with white space to exercise the parsing process
    val whitelistText = """

$iouContract:$hash1,$hash2


""".trimIndent()
    val wls = whitelistText.parseWhitelist()
    assertEquals(1, wls.size)
    assertTrue(wls.containsKey(iouContract))
    val attachmentIds = wls[iouContract]!!
    assertEquals(2, attachmentIds.size)
    assertTrue(attachmentIds.contains(hash1))
    assertTrue(attachmentIds.contains(hash2))
  }

  @Test
  fun `that we can merge whitelists together`() {
    val wl1 = WhitelistSet(mapOf(iouContract to setOf(hash1), tradeContract to setOf(hash2)))
    val wl2 = WhitelistSet(mapOf(tradeContract to setOf(hash3)))
    val wl3 = wl1 + wl2
    val txt = wl3.toString()
    println(txt)
    val wl4 = txt.parseWhitelist()
    assertEquals(2, wl4.size)
    assertTrue(wl3.contains(iouContract))
    assertTrue(wl3.contains(tradeContract))
    assertEquals(1, wl4[iouContract]!!.size)
    assertTrue(wl4[iouContract]!!.contains(hash1))
    assertEquals(2, wl4[tradeContract]!!.size)
    assertTrue(wl4[tradeContract]!!.contains(hash2))
    assertTrue(wl4[tradeContract]!!.contains(hash3))
  }
}