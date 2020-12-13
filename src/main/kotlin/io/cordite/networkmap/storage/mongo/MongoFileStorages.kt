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
package io.cordite.networkmap.storage.mongo

import com.mongodb.reactivestreams.client.MongoClient
import io.cordite.networkmap.keystore.toKeyStore
import io.cordite.networkmap.serialisation.deserializeOnContext
import io.cordite.networkmap.storage.file.CertificateAndKeyPairStorage
import net.corda.core.crypto.SecureHash
import net.corda.nodeapi.internal.SignedNodeInfo
import net.corda.nodeapi.internal.crypto.CertificateAndKeyPair
import net.corda.nodeapi.internal.network.ParametersUpdate
import net.corda.nodeapi.internal.network.SignedNetworkParameters
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.security.KeyPair
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.X509Certificate

class SignedNodeInfoStorage(client: MongoClient, databaseName: String, bucketName: String = DEFAULT_BUCKET_NAME)
  : AbstractMongoFileStorage<SignedNodeInfo>(client, databaseName, bucketName) {
  companion object {
    const val DEFAULT_BUCKET_NAME = "nodes"
  }

  override fun deserialize(data: ByteArray): SignedNodeInfo {
    return data.deserializeOnContext()
  }
}

class ParametersUpdateStorage(client: MongoClient, databaseName: String, bucketName: String = DEFAULT_BUCKET_NAME)
  : AbstractMongoFileStorage<ParametersUpdate>(client, databaseName, bucketName){
  companion object {
    const val DEFAULT_BUCKET_NAME = "parameters-update"
  }

  override fun deserialize(data: ByteArray): ParametersUpdate {
    return data.deserializeOnContext()
  }
}


class SignedNetworkParametersStorage(client: MongoClient, databaseName: String, bucketName: String = DEFAULT_BUCKET_NAME)
  : AbstractMongoFileStorage<SignedNetworkParameters>(client, databaseName, bucketName){
  companion object {
    const val DEFAULT_BUCKET_NAME = "signed-network-parameters"
  }
  
  override fun deserialize(data: ByteArray): SignedNetworkParameters {
    return data.deserializeOnContext()
  }
}

class SecureHashStorage(client: MongoClient, databaseName: String, bucketName: String = DEFAULT_BUCKET_NAME)
  : AbstractMongoFileStorage<SecureHash>(client, databaseName, bucketName){
  companion object {
    const val DEFAULT_BUCKET_NAME = "secure-hash"
  }
  
  override fun deserialize(data: ByteArray): SecureHash {
    return data.deserializeOnContext()
  }
}

class CertificateAndKeyPairStorage(client: MongoClient, databaseName: String, bucketName: String = DEFAULT_BUCKET_NAME, val password: String = DEFAULT_PASSWORD)
  : AbstractMongoFileStorage<CertificateAndKeyPair>(client, databaseName, bucketName){
  companion object {
    const val DEFAULT_BUCKET_NAME = "certs"
    const val DEFAULT_KEY_ALIAS = "key"
    const val DEFAULT_CERT_ALIAS = "cert"
    const val DEFAULT_PASSWORD = "changeme"
  }

  private val pwArray = password.toCharArray()

  override fun deserialize(data: ByteArray): CertificateAndKeyPair {
    val ks = KeyStore.getInstance("JKS")
    ks.load(ByteArrayInputStream(data), pwArray)
    val pk = ks.getKey(DEFAULT_KEY_ALIAS, pwArray) as PrivateKey
    val cert = ks.getCertificate(DEFAULT_CERT_ALIAS) as X509Certificate
    return CertificateAndKeyPair(cert, KeyPair(cert.publicKey, pk))
  }

  override fun serialize(value: CertificateAndKeyPair): ByteBuffer {
    val ks = value.toKeyStore(CertificateAndKeyPairStorage.DEFAULT_CERT_ALIAS, CertificateAndKeyPairStorage.DEFAULT_KEY_ALIAS, password)
    return with(ByteArrayOutputStream()) {
      ks.store(this, pwArray)
      this.toByteArray()
    }.let { ByteBuffer.wrap(it) }
  }

}


