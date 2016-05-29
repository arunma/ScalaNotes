import java.util.concurrent.TimeoutException

import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * @author Arun
  */

class PromisingFuturesTest extends FunSpec with Matchers {

  describe("A PromisingFuture") {

    it("should hold a Int value if the Await.result is called after the Future completes") {
      val promisingFuture = new PromisingFutures()
      val oneFuture = promisingFuture.oneFuture //Takes 1 second to compute
      val intValue = Await.result(oneFuture, 2 seconds)
      intValue should be(1)
    }

    it("should propagate the Exception to the callee if the computation threw an exception") {
      val promisingFuture = new PromisingFutures()
      val oneDangerousFuture = promisingFuture.oneDangerousFuture //throws exception
      intercept[SomeComputationException] {
        val intValue = Await.result(oneDangerousFuture, 2 seconds)
      }
    }

    it("should throw a TimeOutException exception when an Await.result's atMost parameter is lesser than the time taken for the Future to complete") {
      val promisingFuture = new PromisingFutures()
      val oneDelayedFuture = promisingFuture.oneFuture //Takes 1 second to compute
      intercept[TimeoutException] {
        Await.result(oneDelayedFuture, 500 millis)
      }
    }
  }

}
