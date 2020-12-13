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
package io.cordite.networkmap.netty

import io.netty.buffer.ByteBuf
import io.netty.util.CharsetUtil
import io.netty.util.concurrent.FastThreadLocal
import io.netty.util.internal.PlatformDependent
import io.netty.util.internal.StringUtil
import java.nio.charset.Charset

private const val MAX_TL_ARRAY_LEN = 1024

fun ByteBuf.decodeString(charset: Charset): String {
  return decodeString(readerIndex(), readableBytes(), charset)
}

fun ByteBuf.decodeString(readerIndex: Int, len: Int, charset: Charset): String {
  if (len == 0) {
    return StringUtil.EMPTY_STRING
  }
  val (array, offset) = if (hasArray()) {
    array() to arrayOffset() + readerIndex
  } else {
    val a = threadLocalTempArray(len)
    val o = 0
    getBytes(readerIndex, a, 0, len)
    a to o
  }

  if (CharsetUtil.US_ASCII == charset) {
    // Fast-path for US-ASCII which is used frequently.
    return String(array, offset, len)
  }
  return String(array, offset, len, charset)
}

fun threadLocalTempArray(minLength: Int): ByteArray {
  return if (minLength <= MAX_TL_ARRAY_LEN) {
    BYTE_ARRAYS.get()
  } else {
    PlatformDependent.allocateUninitializedArray(minLength)
  }
}

private val BYTE_ARRAYS = object : FastThreadLocal<ByteArray>() {
  override fun initialValue(): ByteArray {
    return PlatformDependent.allocateUninitializedArray(MAX_TL_ARRAY_LEN)
  }
}
