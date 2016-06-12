import java.util.concurrent.{ExecutorService, Executors}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * @author Arun
  */

class PromisingFutures {

  //import scala.concurrent.ExecutionContext.Implicits.global

  implicit lazy val fixedThreadPoolExecutionContext: ExecutionContext = {
    val fixedThreadPool: ExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors * 2)
    ExecutionContext.fromExecutor(fixedThreadPool)
  }

  val oneFuture: Future[Int] = Future {
    Thread.sleep(1000)
    1
  }

  val oneDangerousFuture = Future {
    Thread.sleep(2000)
    throw new SomeComputationException("Welcome to the Dark side !")
  }

  def printFuture[T](future: Future[T]): Unit = future.onComplete {
    case Success(result) => println(s"Success $result")
    case Failure(throwable) => println(s"Failure $throwable")
  }

  def printWithForEach[T](future: Future[T]): Unit = future.foreach(println)

  def checkState(): Unit = {
    println("Before the job finishes")
    Thread.sleep(500)
    println(s"Completed : ${oneFuture.isCompleted}, Value : ${oneFuture.value}")

    println("After the job finishes")
    Thread.sleep(1100)
    println(s"Completed : ${oneFuture.isCompleted}, Value : ${oneFuture.value}")

  }
}


case class SomeComputationException(msg: String) extends Exception(msg)


object PromisingFutures {

  def main(args: Array[String]) {
    val promisingFutures = new PromisingFutures
    //promisingFutures.checkState()
    promisingFutures.printFuture(promisingFutures.oneFuture)
    promisingFutures.printWithForEach(promisingFutures.oneFuture)
    promisingFutures.printFuture(promisingFutures.oneDangerousFuture)
    promisingFutures.printWithForEach(promisingFutures.oneDangerousFuture)

    synchronized(wait(3000))
  }
}
