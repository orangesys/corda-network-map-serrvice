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

import io.bluebank.braid.core.logging.loggerFor
import io.cordite.networkmap.keystore.toKeyStore
import io.cordite.networkmap.storage.file.CertificateAndKeyPairStorage
import io.cordite.networkmap.utils.*
import io.vertx.core.Vertx
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import net.corda.core.crypto.Crypto
import net.corda.nodeapi.internal.crypto.CertificateAndKeyPair
import net.corda.nodeapi.internal.crypto.X509Utilities
import net.corda.nodeapi.internal.crypto.save
import org.junit.*
import org.junit.runner.RunWith
import java.io.File
import java.nio.file.Files

@RunWith(VertxUnitRunner::class)
class CertificateManagerTest {
	companion object {
		private val log = loggerFor<CertificateManagerTest>()
		
		@JvmField
		@ClassRule
		val mdcClassRule = JunitMDCRule()
		
		const val KEYSTORE_PASSWORD = "password"
		
		@JvmStatic
		@BeforeClass
		fun beforeClass() {
			log.info("before class")
			SerializationTestEnvironment.init()
		}
	}
	
	@JvmField
	@Rule
	val mdcRule = JunitMDCRule()
	private val dbDirectory = createTempDir()
	private lateinit var vertx: Vertx
	
	@Before
	fun before() {
		vertx = Vertx.vertx()
	}
	
	@After
	fun after() {
		vertx.close()
	}
	
	@Test
	fun validateNodeInfoCertificates(context: TestContext) {
		val caCertAndKeyPair = createRootCACertAndKeyPair()
		val trustStoreFile = createRootTrustStore(caCertAndKeyPair)
		val certificateManagerConfig = CertificateManagerConfig(
			doorManEnabled = true,
			certManEnabled = true,
			certManPKIVerficationEnabled = true,
			certManRootCAsTrustStoreFile = trustStoreFile,
			certManRootCAsTrustStorePassword = KEYSTORE_PASSWORD,
			certManStrictEVCerts = false)
		val keyStoreDirectory = Files.createTempDirectory("certstore").toFile()
		keyStoreDirectory.deleteOnExit()
		val store = CertificateAndKeyPairStorage(vertx, dbDirectory)
		val certManager = CertificateManager(vertx, store, certificateManagerConfig)
		val async = context.async()
		certManager.init()
			.onSuccess {
				async.complete()
			}
			.catch {
				context.fail(it)
			}
	}
	
	private fun createRootTrustStore(caCertAndKeyPair: CertificateAndKeyPair): File? {
		val trustStore = caCertAndKeyPair.toKeyStore("cert", "key", KEYSTORE_PASSWORD)
		val trustStoreFile = File.createTempFile("truststore", ".jks")
		trustStoreFile.deleteOnExit()
		trustStore.save(trustStoreFile.toPath(), KEYSTORE_PASSWORD)
		return trustStoreFile
	}
	
	private fun createRootCACertAndKeyPair(): CertificateAndKeyPair {
		val caKeyPair = Crypto.generateKeyPair(Crypto.ECDSA_SECP256R1_SHA256)
		val caCert = X509Utilities.createSelfSignedCACertificate(CertificateManagerConfig.DEFAULT_ROOT_NAME.x500Principal, caKeyPair)
		return CertificateAndKeyPair(caCert, caKeyPair)
	}
}