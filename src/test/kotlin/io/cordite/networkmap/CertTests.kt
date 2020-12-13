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
package io.cordite.networkmap

import io.cordite.networkmap.keystore.toKeyStore
import io.cordite.networkmap.keystore.toX509KeyStore
import io.cordite.networkmap.utils.JunitMDCRule
import net.corda.core.crypto.Crypto
import net.corda.core.identity.CordaX500Name
import net.corda.nodeapi.internal.DEV_ROOT_CA
import net.corda.nodeapi.internal.crypto.CertificateAndKeyPair
import net.corda.nodeapi.internal.crypto.CertificateType
import net.corda.nodeapi.internal.crypto.X509Utilities
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue

operator fun Path.div(other: String): Path = resolve(other)
operator fun File.div(other: String): File = File(this, other)

class CertTests {
  companion object {
    @JvmField
    @ClassRule
    val mdcClassRule = JunitMDCRule()

    private val certificateDirectory = File("src/test/resources/certificates")
    val nodeKeyStorePath = certificateDirectory / "nodekeystore.jks"
    const val password = "cordacadevpass"
  }

  @JvmField
  @Rule
  val mdcRule = JunitMDCRule()

  @Test
  fun test1() {
    println(System.getProperty("user.dir"))
    nodeKeyStorePath.toX509KeyStore(password)
  }

  @Test
  fun `generate a JKS with cert and private key`() {
    val password = "password"
    val signatureScheme = Crypto.ECDSA_SECP256R1_SHA256
    val certificateType = CertificateType.LEGAL_IDENTITY
    val keyPair = Crypto.generateKeyPair(signatureScheme)
    val rootCa = DEV_ROOT_CA
    val name = CordaX500Name("myorg.org", "Unit in My Org", "My Org", "London", "London", "GB")
    val cert = X509Utilities.createCertificate(
      certificateType,
      rootCa.certificate,
      rootCa.keyPair,
      name.x500Principal,
      keyPair.public)
    val certAndKey = CertificateAndKeyPair(cert, keyPair)
    val keyStore = certAndKey.toKeyStore("cert-alias", "key-alias", password, certPath = listOf(rootCa.certificate))
    val aliases = keyStore.aliases().toList()
    assertTrue(aliases.contains("cert-alias"))
    assertTrue(aliases.contains("key-alias"))
    val certificate = keyStore.getCertificate("cert-alias")
    assertEquals(cert, certificate)
  }
}
