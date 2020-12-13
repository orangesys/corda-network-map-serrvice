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

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.impl.NoStackTraceThrowable
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import org.junit.After
import org.junit.ClassRule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File


@RunWith(VertxUnitRunner::class)
class VertxKtTests {
  companion object {
    @JvmField
    @ClassRule
    val mdcClassRule = JunitMDCRule()
  }

  private val vertx = Vertx.vertx()
  @After
  fun after() {
    vertx.close()
  }

  @Test
  fun `test delete file`(context: TestContext) {
    val file = File.createTempFile("delete-me", ".txt")
    val async = context.async()
    vertx.fileSystem().deleteFile(file.absolutePath)
      .onSuccess { async.complete() }
      .catch(context::fail)
  }

  @Test
  fun `that future failure can be caught`(context: TestContext) {
    val async = context.async()
    Future.failedFuture<String>("failed")
      .onSuccess {
        context.fail()
      }
      .catch {
        async.complete()
      }
  }

  @Test
  fun `that future catch handler can throw an exception`(context: TestContext) {
    val async = context.async()
    Future.failedFuture<String>("failed")
      .catch {
        throw RuntimeException("double fail")
      }
      .onSuccess {
        context.fail()
      }
      .catch {
        if (it.message == "double fail") {
          async.complete()
        } else {
          context.fail()
        }
      }
  }

  @Test
  fun `that future success handler can throw an exception`(context: TestContext) {
    val async = context.async()
    Future.succeededFuture("hello")
      .onSuccess {
        throw RuntimeException("failed")
      }
      .catch {
        if (it.message == "failed") {
          async.complete()
        } else {
          context.fail()
        }
      }
  }

  @Test
  fun `that a double complete on a future list works as expected`(context: TestContext) {
    val async = context.async()
    val f1 = JiggedFuture<Int>()
    val f2 = Future.succeededFuture(2)
    listOf(f1, f2).all()
      .onSuccess {
        context.assertEquals(1, it[0])
        context.assertEquals(2, it[1])
        async.complete()
      }
      .catch(context::fail)
    f1.complete(1)
    f1.complete(2) // double complete!
  }
}


/**
 * Create a future that hasn't completed yet
 */
private class JiggedFuture<T> : Future<T>, Handler<AsyncResult<T>> {
  private var failed: Boolean = false
  private var succeeded: Boolean = false
  private var handler: Handler<AsyncResult<T>>? = null
  private var result: T? = null
  private var throwable: Throwable? = null

  /**
   * The result of the operation. This will be null if the operation failed.
   */
  override fun result(): T? {
    return result
  }

  /**
   * An exception describing failure. This will be null if the operation succeeded.
   */
  override fun cause(): Throwable? {
    return throwable
  }

  /**
   * Did it succeeed?
   */
  @Synchronized
  override fun succeeded(): Boolean {
    return succeeded
  }

  /**
   * Did it fail?
   */
  @Synchronized
  override fun failed(): Boolean {
    return failed
  }

  /**
   * Has it completed?
   */
  @Synchronized
  override fun isComplete(): Boolean {
    return failed || succeeded
  }

  /**
   * Set a handler for the result. It will get called when it's complete
   */
  override fun setHandler(handler: Handler<AsyncResult<T>>): Future<T> {
    var callHandler = false
    synchronized(this) {
      this.handler = handler
      callHandler = isComplete
    }
    if (callHandler) {
      handler.handle(this)
    }
    return this
  }

  override fun complete(result: T) {
    if (!tryComplete(result)) {
//      throw IllegalStateException("Result is already complete: " + if (succeeded) "succeeded" else "failed")
    }
  }

  override fun complete() {
    if (!tryComplete()) {
//      throw IllegalStateException("Result is already complete: " + if (succeeded) "succeeded" else "failed")
    }
  }

  override fun fail(cause: Throwable) {
    if (!tryFail(cause)) {
//      throw IllegalStateException("Result is already complete: " + if (succeeded) "succeeded" else "failed")
    }
  }

  override fun fail(failureMessage: String) {
    if (!tryFail(failureMessage)) {
//      throw IllegalStateException("Result is already complete: " + if (succeeded) "succeeded" else "failed")
    }
  }

  override fun tryComplete(result: T?): Boolean {
    var h: Handler<AsyncResult<T>>? = null
    synchronized(this) {
      //      if (succeeded || failed) {
//        return false
//      }
      this.result = result
      succeeded = true
      h = handler
    }
    if (h != null) {
      h!!.handle(this)
    }
    return true
  }

  override fun tryComplete(): Boolean {
    return tryComplete(null)
  }

  fun handle(ar: Future<T>) {
    if (ar.succeeded()) {
      complete(ar.result())
    } else {
      fail(ar.cause())
    }
  }

  override fun completer(): Handler<AsyncResult<T>> {
    return this
  }

  override fun handle(asyncResult: AsyncResult<T>) {
    if (asyncResult.succeeded()) {
      complete(asyncResult.result())
    } else {
      fail(asyncResult.cause())
    }
  }

  override fun tryFail(cause: Throwable?): Boolean {
    var h: Handler<AsyncResult<T>>? = null
    synchronized(this) {
      if (succeeded || failed) {
        return false
      }
      this.throwable = cause ?: NoStackTraceThrowable(null)
      failed = true
      h = handler
    }
    if (h != null) {
      h!!.handle(this)
    }
    return true
  }

  override fun tryFail(failureMessage: String): Boolean {
    return tryFail(NoStackTraceThrowable(failureMessage))
  }

  override fun toString(): String {
    synchronized(this) {
      if (succeeded) {
        return "Future{result=$result}"
      }
      return if (failed) {
        "Future{cause=" + throwable!!.message + "}"
      } else "Future{unresolved}"
    }
  }
}
