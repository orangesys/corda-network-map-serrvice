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

import io.bluebank.braid.corda.serialisation.BraidCordaJacksonInit
import io.vertx.core.json.Json
import net.corda.core.node.NetworkParameters
import org.junit.Test
import kotlin.test.assertEquals

class NMPMixinTest {
	@Test
	fun `that we can deserialize NMP with default values`() {
		BraidCordaJacksonInit.init()
		Json.mapper.addMixIn(NetworkParameters::class.java, NetworkParametersMixin::class.java)
		Json.prettyMapper.addMixIn(NetworkParameters::class.java, NetworkParametersMixin::class.java)
		val nmpString = Json.mapper.writeValueAsString(NetworkParametersMixin())
		val networkParameters: NetworkParameters = Json.mapper.readValue(nmpString, NetworkParameters::class.java)
		assertEquals(networkParameters.epoch, 1 )
	}
}