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
package io.cordite.networkmap.storage.mongo.serlalisation

import com.fasterxml.jackson.databind.ObjectMapper
import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.RawBsonDocument
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistry
import java.io.IOException
import java.io.UncheckedIOException

internal class JacksonCodec<T>(private val bsonObjectMapper: ObjectMapper,
                               codecRegistry: CodecRegistry,
                               private val type: Class<T>) : Codec<T> {
  private val rawBsonDocumentCodec: Codec<RawBsonDocument> = codecRegistry.get(RawBsonDocument::class.java)

  override fun decode(reader: BsonReader, decoderContext: DecoderContext): T {
    try {
      val document = rawBsonDocumentCodec.decode(reader, decoderContext)
      return bsonObjectMapper.readValue(document.byteBuffer.array(), type)
    } catch (e: IOException) {
      throw UncheckedIOException(e)
    }
  }

  override fun encode(writer: BsonWriter?, value: T, encoderContext: EncoderContext?) {
    try {
      val data = bsonObjectMapper.writeValueAsBytes(value)
      rawBsonDocumentCodec.encode(writer, RawBsonDocument(data), encoderContext)
    } catch (e: IOException) {
      throw UncheckedIOException(e)
    }
  }

  override fun getEncoderClass(): Class<T> {
    return this.type
  }
}