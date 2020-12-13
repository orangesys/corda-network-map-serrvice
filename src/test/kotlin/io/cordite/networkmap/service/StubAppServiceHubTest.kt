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
package io.cordite.networkmap.service

import io.cordite.networkmap.utils.JunitMDCRule
import net.corda.core.contracts.StateRef
import net.corda.core.crypto.SecureHash
import net.corda.core.flows.FlowLogic
import net.corda.core.node.StatesToRecord
import net.corda.core.serialization.SerializeAsToken
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test
import java.util.function.Consumer

class StubAppServiceHubTest {
  companion object {
    @JvmField
    @ClassRule
    val mdcClassRule = JunitMDCRule()
  }

  @JvmField
  @Rule
  val mdcRule = JunitMDCRule()

  @Test
  fun `capture all the dead code for coverage`() {
    StubAppServiceHub().apply {
      ignore { attachments }
      ignore { clock }
      ignore { contractUpgradeService }
      ignore { cordappProvider }
      ignore { identityService }
      ignore { keyManagementService }
      ignore { myInfo }
      ignore { networkMapCache }
      ignore { networkParameters }
      ignore { transactionVerifierService }
      ignore { validatedTransactions }
      ignore { vaultService }
      ignore { networkParametersService }
      ignore { cordaService(SerializeAsToken::class.java) }
      ignore { jdbcSession() }
      ignore { loadState(StateRef(SecureHash.zeroHash, 0)) }
      ignore { loadStates(setOf()) }
      ignore { recordTransactions(StatesToRecord.NONE, listOf()) }
      ignore { registerUnloadHandler { } }
      ignore {
        startFlow(object : FlowLogic<Int>() {
          override fun call(): Int {
            return 0
          }
        })
      }
      ignore {
        startTrackedFlow(object : FlowLogic<Int>() {
          override fun call(): Int {
            return 0
          }
        })
      }
      ignore { withEntityManager { } }
      ignore {
        withEntityManager(Consumer { })
      }
    }
  }

  private fun StubAppServiceHub.ignore(fn: StubAppServiceHub.() -> Unit) {
    try {
      fn()
    } catch (err: Throwable) {
      // ignore
    }
  }
}