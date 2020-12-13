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

import io.cordite.networkmap.keystore.toJksOptions
import io.vertx.core.net.JksOptions
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.FileReader
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.Certificate
import java.util.*
import javax.xml.bind.DatatypeConverter

class CertsToJksOptionsConverter(private val certificatePath: String, private val privateKeyPath: String) {
  private val factory: CertificateFactory by lazy { CertificateFactory() }

  internal val keyStore: KeyStore by lazy {
    KeyStore.getInstance("JKS", "SUN").apply {
      load(null, keyStorePassword.toCharArray())
      val privateKey = loadPrivateKeyFile()
      val cert = createCertificate()
      setKeyEntry(privateKeyPath, privateKey, keyStorePassword.toCharArray(), cert)
    }
  }

  internal val keyStorePassword = UUID.randomUUID().toString()

  fun createJksOptions(): JksOptions {
    return keyStore.toJksOptions(keyStorePassword)
  }

  private fun createCertificate(): Array<Certificate> {
    val result = ArrayList<Certificate>()
    val r = BufferedReader(FileReader(certificatePath))
    var s: String? = r.readLine()
    if (s == null || !s.contains("BEGIN CERTIFICATE")) {
      r.close()
      throw IllegalArgumentException("No CERTIFICATE found")
    }
    var b = StringBuilder()
    while (s != null) {
      if (s.contains("END CERTIFICATE")) {
        val hexString = b.toString()
        val bytes = DatatypeConverter.parseBase64Binary(hexString)
        val cert = generateCertificateFromDER(bytes)
        result.add(cert)
        b = StringBuilder()
      } else {
        if (!s.startsWith("----")) {
          b.append(s)
        }
      }
      s = r.readLine()
    }
    r.close()
    return result.toTypedArray()
  }

  private fun generateCertificateFromDER(certBytes: ByteArray): Certificate {
    return factory.engineGenerateCertificate(ByteArrayInputStream(certBytes))
  }

  private fun loadPrivateKeyFile(): PrivateKey {
    val pemParser = PEMParser(FileReader(privateKeyPath))
    val keyObject = pemParser.readObject()
    val converter = JcaPEMKeyConverter()
    return when (keyObject) {
      is PEMKeyPair -> {
        converter.getKeyPair(keyObject).private
      }
      is PrivateKeyInfo -> {
        converter.getPrivateKey(keyObject)
      }
      else -> {
        throw RuntimeException("unsupported key object ${keyObject.javaClass}")
      }
    }
  }
}