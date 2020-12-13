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

import net.corda.core.identity.CordaX500Name
import org.bouncycastle.asn1.x500.X500Name
import java.security.cert.CertPathValidator
import java.security.cert.X509Certificate


class CertificateRequestPayload(
  private val certs: List<X509Certificate>,
  private val signature: ByteArray,
  private val certificateManagerConfig: CertificateManagerConfig
) {
  companion object {
    private val certPathValidator = CertPathValidator.getInstance("PKIX")
  }

  val x500Name: CordaX500Name by lazy {
    val x500 = X500Name.getInstance(certs.first().subjectX500Principal.encoded)
    x500.toCordaX500Name(certificateManagerConfig.certManStrictEVCerts)
  }

  fun verify() {
    certs.forEach { it.checkValidity() }
    if (certificateManagerConfig.certManPKIVerficationEnabled) {
      verifyPKIPath()
    }
    verifySignature()
  }

  private fun verifyPKIPath() {
    val certPath = certificateManagerConfig.certFactory.generateCertPath(certs)
    certPathValidator.validate(certPath, certificateManagerConfig.pkixParams)
  }

  private fun verifySignature() {
    CertificateManager.createSignature().apply {
      initVerify(certs.first())
      verify(signature)
    }
  }
}

