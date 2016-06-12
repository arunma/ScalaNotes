import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * @author Arun
  */
class FutureCombinatorsTest extends FunSpec with Matchers with ConcurrentUtils {

  describe("Futures that are executed sequentially") {
    it("could be composed using map") {
      val futureCombinators = new FutureCombinators
      val result = timed(Await.result(futureCombinators.sumOfThreeNumbersSequentialMap(), 7 seconds))
      result shouldBe 6
    }

    it("could be composed using for comprehensions") {
      val futureCombinators = new FutureCombinators
      val result = timed(Await.result(futureCombinators.sumOfThreeNumbersSequentialForComprehension(), 7 seconds))
      result shouldBe 6
    }
  }

  describe("Futures that are executed in parallel") {
    it("could be composed using for comprehensions") {
      val futureCombinators = new FutureCombinators
      val result = timed(Await.result(futureCombinators.sumOfThreeNumbersParallel(), 4 seconds))
      result shouldBe 6
    }

    it("could be composed using for comprehensions respecting the guard for the generator") {
      val futureCombinators = new FutureCombinators
      val result = timed(Await.result(futureCombinators.sumOfThreeNumbersParallelWithGuard(), 4 seconds))
      result shouldBe 6
    }

    it("could be composed using async/await") {
      val futureCombinators = new FutureCombinators
      val result = timed(Await.result(futureCombinators.sumOfThreeNumbersParallelWithAsyncAwait(), 4 seconds))
      result shouldBe 6
    }
  }

  /** Exception handling **/
  describe("Futures that throw exception") {
    it("could blow up on the caller code when guard fails") {
      val futureCombinators = new FutureCombinators
      intercept[NoSuchElementException] {
        val result = timed(Await.result(futureCombinators.throwsNoSuchElementIfGuardFails(), 4 seconds))
      }
    }

    it("could blow up on the caller code when exception comes from a computation executed inside the Future") {
      val futureCombinators = new FutureCombinators
      intercept[LegacyException] {
        val result = timed(Await.result(futureCombinators.throwsExceptionFromComputation(), 4 seconds))
      }
    }

    it("could be recovered with a recovery value") {
      val futureCombinators = new FutureCombinators
      val result = timed(Await.result(futureCombinators.recoversFromExceptionUsingRecover(), 2 seconds))
      result shouldBe 201
    }

    it("could be recovered with a recovery Future") {
      val futureCombinators = new FutureCombinators
      val result = timed(Await.result(futureCombinators.recoversFromExceptionUsingRecoverWith(), 4 seconds))
      result shouldBe 1001
    }

    it("when recovered with another Future that throws Exception would throw the error from the second Future") {
      val futureCombinators = new FutureCombinators
      val exception = intercept[LegacyException] {
        timed(Await.result(futureCombinators.recoversFromExceptionUsingRecoverWithThatFails(), 4 seconds))
      }
      exception.msg shouldBe "Dieded!!"
    }

    it("when fallen back to another Future that throws Exception would throw the error from the first Future") {
      val futureCombinators = new FutureCombinators
      val exception = intercept[LegacyException] {
        timed(Await.result(futureCombinators.recoversFromExceptionUsingFallbackTo(), 4 seconds))
      }
      exception.msg shouldBe "Danger! Danger!"
    }
  }

}

trait ConcurrentUtils {
  def timed[T](block: => T): T = {
    val start = System.currentTimeMillis()
    val result = block
    val duration = System.currentTimeMillis() - start
    println(s"Time taken : $duration")
    result
  }
}