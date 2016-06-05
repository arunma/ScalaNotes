import java.util.concurrent.Executors

import scala.concurrent.{Future, Promise}
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

/**
  * @author Arun
  */

class PromiseInternals {

  def aCompletedPromiseUsingSuccess(num: Int): Future[Int] = {
    val promise = Promise[Int]()
    promise.success(num)
    promise.future
  }

  def aCompletedPromiseUsingComplete(num: Int): Future[Int] = {
    val promise = Promise[Int]()
    promise.complete(Success(num))
    promise.future
  }

  def aCompletedPromiseUsingFailure(num: Int): Future[Int] = {
    val promise = Promise[Int]()
    promise.failure(new RuntimeException("Evil Exception"))
    promise.future
  }

  /**
    * This would return a KeptPromise because it has a value that is already resolved
    *
    * @return
    */
  def valueSetOnCreation(num: Int): Future[Int] = {
    val promise = Promise.successful(num)
    promise.future
  }

  /**
    * This would return a DefaultPromise
    *
    * @return
    */

  val somePool = Executors.newFixedThreadPool(2)

  def someExternalDelayedCalculation(f: () => Int): Future[Int] = {
    val promise = Promise[Int]()
    val thisIsWhereWeCallSomeExternalComputation = new Runnable {
      override def run(): Unit = {
        promise.complete {
          try (Success(f()))
          catch {
            case NonFatal(msg) => Failure(msg)
          }
        }
      }
    }

    somePool.execute(thisIsWhereWeCallSomeExternalComputation)
    promise.future
  }


  def alreadyCompletedPromise(): Future[Int] = {
    val promise = Promise[Int]()
    promise.success(100) //completed
    promise.failure(new RuntimeException("Will never be set because an IllegalStateException will be thrown beforehand"))
    promise.future
  }

}
