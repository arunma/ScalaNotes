import scala.async.Async._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * @author Arun
  */
class FutureCombinators {

  def sumOfThreeNumbersSequentialMap(): Future[Int] = {
    Future {
      Thread.sleep(1000)
      1
    }.flatMap { oneValue =>
      Future {
        Thread.sleep(2000)
        2
      }.flatMap { twoValue =>
        Future {
          Thread.sleep(3000)
          3
        }.map { thirdValue =>
          oneValue + twoValue + thirdValue
        }
      }
    }
  }

  def sumOfThreeNumbersSequentialForComprehension(): Future[Int] = {
    for {
      localOne <- Future {
        Thread.sleep(1000)
        1
      }
      localTwo <- Future {
        Thread.sleep(2000)
        2
      }
      localThree <- Future {
        Thread.sleep(3000)
        3
      }
    } yield localOne + localTwo + localThree
  }

  /** Declare Futures outside **/

  val oneFuture: Future[Int] = Future {
    Thread.sleep(1000)
    1
  }

  val twoFuture: Future[Int] = Future {
    Thread.sleep(2000)
    2
  }

  val threeFuture: Future[Int] = Future {
    Thread.sleep(3000)
    3
  }

  def sumOfThreeNumbersParallel(): Future[Int] = for {
    oneValue <- oneFuture
    twoValue <- twoFuture
    threeValue <- threeFuture
  } yield oneValue + twoValue + threeValue

  def sumOfThreeNumbersMapAndFlatMap(): Future[Int] = oneFuture.flatMap { oneValue =>
    twoFuture.flatMap { twoValue =>
      threeFuture.map { threeValue =>
        oneValue + twoValue + threeValue
      }
    }
  }

  def sumOfThreeNumbersParallelWithGuard(): Future[Int] = for {
    oneValue <- oneFuture
    twoValue <- twoFuture if twoValue > 1
    threeValue <- threeFuture
  } yield oneValue + twoValue + threeValue

  def sumOfThreeNumbersMapAndFlatMapWithFilter(): Future[Int] = oneFuture.flatMap { oneValue =>
    twoFuture.withFilter(_ > 1).flatMap { twoValue =>
      threeFuture.map { threeValue =>
        oneValue + twoValue + threeValue
      }
    }
  }


  /** Exception handling **/

  def throwsNoSuchElementIfGuardFails(): Future[Int] = for {
    oneValue <- oneFuture
    twoValue <- twoFuture if twoValue > 2
    threeValue <- threeFuture
  } yield oneValue + twoValue + threeValue

  val futureCallingLegacyCode: Future[Int] = Future {
    Thread.sleep(1000)
    throw new LegacyException("Danger! Danger!")
  }

  def throwsExceptionFromComputation(): Future[Int] = for {
    oneValue <- oneFuture
    futureThrowingException <- futureCallingLegacyCode
  } yield oneValue + futureThrowingException


  val futureCallingLegacyCodeWithRecover: Future[Int] = futureCallingLegacyCode.recover {
    case LegacyException(msg) => 200
  }

  def recoversFromExceptionUsingRecover(): Future[Int] = for {
    oneValue <- oneFuture
    futureThrowingException <- futureCallingLegacyCodeWithRecover
  } yield oneValue + futureThrowingException


  val futureCallingLegacyCodeWithRecoverWith: Future[Int] = futureCallingLegacyCode.recoverWith {
    case LegacyException(msg) =>
      println("Exception occurred. Recovering with a Future that wraps 1000")
      Thread.sleep(2000)
      Future(1000)
  }

  def recoversFromExceptionUsingRecoverWith(): Future[Int] = for {
    oneValue <- oneFuture
    futureThrowingException <- futureCallingLegacyCodeWithRecoverWith
  } yield oneValue + futureThrowingException

  val anotherErrorThrowingFuture: Future[Int] = Future {
    Thread.sleep(1000)
    throw new LegacyException("Dieded!!")
  }

  val futureRecoveringWithAnotherErrorThrowingFuture: Future[Int] = futureCallingLegacyCode.recoverWith {
    case LegacyException(msg) =>
      println("Exception occurred. Recovering with Another Failure Future")
      anotherErrorThrowingFuture
  }

  def recoversFromExceptionUsingRecoverWithThatFails(): Future[Int] = for {
    oneValue <- oneFuture
    futureThrowingException <- futureRecoveringWithAnotherErrorThrowingFuture
  } yield oneValue + futureThrowingException


  val futureFallingBackToAnotherErrorThrowingFuture: Future[Int] = futureCallingLegacyCode.fallbackTo(anotherErrorThrowingFuture)

  def recoversFromExceptionUsingFallbackTo(): Future[Int] = for {
    oneValue <- oneFuture
    futureThrowingException <- futureFallingBackToAnotherErrorThrowingFuture
  } yield oneValue + futureThrowingException

  def sumOfThreeNumbersParallelWithAsyncAwait(): Future[Int] = async {
    await(oneFuture) + await(twoFuture) + await(threeFuture)
  }

  /** Other popular combinators **/

  //Zip
  def zipTwoFutures: Future[(Int, Int)] = oneFuture zip twoFuture

  //First completedOf
  val listOfFutures = List(oneFuture, twoFuture, threeFuture)

  def getFirstResult(): Future[Int] = Future.firstCompletedOf(listOfFutures)

  //Sequence
  def getResultsAsList(): Future[List[Int]] = Future.sequence(listOfFutures)

}

case class LegacyException(msg: String) extends Exception(msg)


