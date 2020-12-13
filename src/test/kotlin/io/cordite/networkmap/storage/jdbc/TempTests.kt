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
package io.cordite.networkmap.storage.jdbc

import io.cordite.networkmap.serialisation.deserializeOnContext
import io.cordite.networkmap.serialisation.serializeOnContext
import io.cordite.networkmap.utils.JunitMDCRule
import io.cordite.networkmap.utils.SerializationTestEnvironment
import net.corda.core.node.NetworkParameters
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.*
import java.sql.Connection
import java.time.Instant
import kotlin.test.assertEquals


@Ignore
open class KeyValueTable<T : Any>(private val clazz: Class<T>) : Table() {
  private val key = varchar("key", 1024).primaryKey()
  private val value = blob("value")

  open fun assign(updateBuilder: UpdateBuilder<Int>, connection: Connection, key: String, value : T) {
    updateBuilder[this.key] = key
    val blob = connection.createBlob()
    blob.setBytes(1, value.serializeOnContext().bytes)
    updateBuilder[this.value] = blob
  }

  fun extract(row: ResultRow) : Pair<String, T> {
    val k = row[this.key]
    val v = row[this.value].deserializeOnContext(clazz)
    return k to v
  }
}

object NetworkParametersTable : KeyValueTable<NetworkParameters>(NetworkParameters::class.java) {
  // additional fields, that may be made indexed, unique etc
  val minimumVersion = integer("minimum_version").index("minimum_version_idx")

  // we override the assign method to add the behaviour for the extracted and queryable fields
  override fun assign(updateBuilder: UpdateBuilder<Int>, connection: Connection, key: String, value: NetworkParameters) {
    updateBuilder[minimumVersion] = value.minimumPlatformVersion
    super.assign(updateBuilder, connection, key, value)
  }
}

class TempTests {
  companion object {
    @JvmField
    @ClassRule
    val mdcClassRule = JunitMDCRule()
  }

  @JvmField
  @Rule
  val mdcRule = JunitMDCRule()

  @Before
  fun before() {
    SerializationTestEnvironment.init()
  }

  @Test
  fun `save a simple binary payload and retrieve it`() {
    val k1 = "foo"
    val v1 = NetworkParameters(
      minimumPlatformVersion = 3,
      notaries = listOf(),
      maxMessageSize = 1000,
      maxTransactionSize = 2000,
      modifiedTime = Instant.now(),
      epoch = 1,
      whitelistedContractImplementations = mapOf()
    )
    val db = Database.connect(url = "jdbc:h2:mem:test", driver = "org.h2.Driver")
    transaction(db) {
      SchemaUtils.create(NetworkParametersTable)
      NetworkParametersTable.insert {
        assign(it, db.connector(), k1, v1)
      }
      val result = NetworkParametersTable.select { NetworkParametersTable.minimumVersion.eq(v1.minimumPlatformVersion)}.first()
      val (k2, v2) = NetworkParametersTable.extract(result)
      assertEquals(k1, k2)
      assertEquals(v1, v2)
    }
    TransactionManager.closeAndUnregister(db)
  }
}