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

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.whenever
import net.corda.core.internal.staticField
import net.corda.core.serialization.SerializationFactory
import net.corda.core.serialization.internal.SerializationEnvironment
import net.corda.core.serialization.internal.effectiveSerializationEnv
import net.corda.testing.internal.*
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnector
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * A test serialization rule implementation for use in tests
 *
 * @param inheritable whether new threads inherit the environment, use sparingly.
 */
class SerializationEnvironmentRule(private val inheritable: Boolean = false) : TestRule {
    companion object {
        init {
            // Can't turn it off, and it creates threads that do serialization, so hack it:
            InVMConnector::class.staticField<ExecutorService>("threadPoolExecutor").value = rigorousMock<ExecutorService>().also {
                doAnswer {
                    inVMExecutors.computeIfAbsent(effectiveSerializationEnv) {
                        Executors.newCachedThreadPool(testThreadFactory(true)) // Close enough to what InVMConnector makes normally.
                    }.execute(it.arguments[0] as Runnable)
                }.whenever(it).execute(any())
            }
        }
    }

    private lateinit var env: SerializationEnvironment

    val serializationFactory: SerializationFactory get() = env.serializationFactory

    override fun apply(base: Statement, description: Description): Statement {
        env = createTestSerializationEnv()
        return object : Statement() {
            override fun evaluate() = env.asTestContextEnv(inheritable) { base.evaluate() }
        }
    }
}
