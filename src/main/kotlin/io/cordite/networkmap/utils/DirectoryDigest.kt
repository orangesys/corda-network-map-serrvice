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

import io.vertx.core.Future
import io.vertx.core.Vertx
import net.corda.core.utilities.toHexString
import java.io.File
import java.io.FileInputStream
import java.io.SequenceInputStream
import java.security.DigestInputStream
import java.security.MessageDigest

class DirectoryDigest(private val path: File,
                      private val regex: Regex = ".*".toRegex(),
                      private val digestAlgorithm: String = "SHA-256") {

  fun digest(): String {
    val fileStreams = path.getFiles(regex).map { FileInputStream(it) }

    return DigestInputStream(
      SequenceInputStream(fileStreams.toEnumeration()),
      MessageDigest.getInstance(digestAlgorithm)).use {
      while (it.read() > -1) {
      }
      it.messageDigest.digest().toHexString()
    }
  }

  fun digest(vertx: Vertx): Future<String> {
    return vertx.executeBlocking {
      digest()
    }
  }
}


