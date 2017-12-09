package scribe

import java.io.Closeable
import java.util.concurrent._
import java.util.concurrent.atomic.{AtomicInteger, AtomicReference}
import scala.language.experimental.macros
import scala.util.control.NonFatal

class AsyncLoggerSupport(innerLog: LogRecord => Unit) extends Closeable {

  private[scribe] val queue = new LinkedBlockingQueue[Option[LogRecord]]()

  private[scribe] var ec: ExecutorService = _

  private[scribe] val complete = new AnyRef

  private[scribe] val state = new AtomicReference[AsyncLoggerSupport.State](AsyncLoggerSupport.Init)

  start()

  protected[scribe] def log(record: LogRecord): Unit = {
    queue.add(Some(record))
  }

  private[scribe] final def run(): Unit = {
    var here: Option[LogRecord] = None

    while ({here = queue.poll(Long.MaxValue, TimeUnit.NANOSECONDS); here.nonEmpty}) {
      try innerLog(here.get)
      catch {
        case NonFatal(e) =>
          //TODO: Handle logging errors.
      }
    }

    complete.synchronized {
      //let close() know we're done
      complete.notify()
    }
  }

  private[scribe] def start(): Unit = {
    if (state.compareAndSet(AsyncLoggerSupport.Init, AsyncLoggerSupport.Running)) {
      ec = Executors.newSingleThreadExecutor(AsyncLoggerSupport.threadFactory)

      ec.execute(new Runnable {
        override def run(): Unit = {
          AsyncLoggerSupport.this.run()
        }
      })

      Runtime.getRuntime.addShutdownHook(
        new Thread(new Runnable {
          override def run(): Unit = {
            AsyncLoggerSupport.this.close()
          }
        })
      )
    }
  }

  override def close(): Unit = {
    if (state.compareAndSet(AsyncLoggerSupport.Running, AsyncLoggerSupport.Stopped)) {
      queue.put(None)
      complete.synchronized {
        //wait for run() to complete
        complete.wait()
      }
      ec.shutdown()
    }
  }

}

private object AsyncLoggerSupport {
  val threadCounter = new AtomicInteger()

  val threadFactory =
    new ThreadFactory {
      override def newThread(r: Runnable): Thread = {
        val t = new Thread(r)
        t.setName("AsyncLoggerSupport-" + AsyncLoggerSupport.threadCounter.getAndIncrement())
        t.setDaemon(true)
        t
      }
    }

  sealed trait State

  case object Init extends State

  case object Running extends State

  case object Stopped extends State
}
