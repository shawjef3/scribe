package specs

import org.scalatest.FunSuite
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import scribe.{AsyncLoggerSupport, LogHandler}

class AsyncLoggerSupportSpec extends FunSuite {

  test("log in another thread and shutdown") {
scribe.enableAsynchonous()
    val async = new AsyncLoggerSupport {
      override def name: Option[String] = Some("AsyncLoggerSupportSpec")

      override def parentName: Option[String] = None

      override def multiplier: Double = 1.0
    }

    async.addHandler(LogHandler())

    Future {
      for (i <- 0 until 10) {
        Future {
          async.info(i)
        }
      }
    }

  }

}
