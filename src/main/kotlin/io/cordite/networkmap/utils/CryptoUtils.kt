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

import net.corda.core.crypto.Crypto
import net.corda.core.internal.SignedDataWithCert
import net.corda.core.internal.signWithCert
import net.corda.nodeapi.internal.crypto.CertificateAndKeyPair
import java.security.PrivateKey
import java.util.*

object CryptoUtils {
  fun decodePEMPrivateKey(pem: String): PrivateKey {
    val encodedPrivKey = Base64.getDecoder().decode(pem.lines().filter { !it.startsWith("---") }.joinToString(separator = ""))
    return Crypto.decodePrivateKey(encodedPrivKey)
  }
}

inline fun <reified T : Any> T.sign(certs: CertificateAndKeyPair): SignedDataWithCert<T> {
  return signWithCert(certs.keyPair.private, certs.certificate)
}
