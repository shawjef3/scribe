package scribe

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object Main extends App {
  scribe.enableAsynchronous()

  Future {
    for (i <- 0 to Int.MaxValue) {
      scribe.info(s"forgotten $i")
      Thread.sleep(1L)
    }
  }

  Await.ready(
    Future {
      for (i <- 0 to 1000)
        scribe.info(s"awaited $i")
    },
    Duration.Inf
  )

  scribe.info("done")
  scribe.flush()
}
