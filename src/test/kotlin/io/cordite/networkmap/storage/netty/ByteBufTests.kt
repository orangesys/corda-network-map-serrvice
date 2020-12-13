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
package io.cordite.networkmap.storage.netty

import io.cordite.networkmap.utils.JunitMDCRule
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import org.junit.Rule
import org.junit.Test
import java.nio.ByteBuffer
import kotlin.test.assertEquals

class ByteBufTests {

  @JvmField
  @Rule
  val mdcRule = JunitMDCRule()

  @Test
  fun `does bytebuf track bytebuffer`() {
    val byteBuffer = ByteBuffer.allocate(100)
    byteBuffer.write("hello world")
    byteBuffer.flip()
    val byteBuf = Unpooled.wrappedBuffer(byteBuffer).slice(0, 5)
    try {
      val result = byteBuf.readString(5)
      assertEquals("hello", result)
    } finally {
      byteBuf.release()
    }
  }

  private fun ByteBuffer.write(str: String) {
    str.apply {
      forEach { put(it.toByte()) }
    }
  }

  private fun ByteBuf.readString(length: Int): String {
    return readCharSequence(length, Charsets.US_ASCII).toString()
  }
}

