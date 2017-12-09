import java.io.Closeable
import java.util.concurrent.atomic.AtomicBoolean
import scala.language.experimental.macros

package object scribe extends Root with Closeable {
  private val asynchronousEnabled = new AtomicBoolean(false)
  @volatile
  private var _asynchronous: AsyncLoggerSupport = null

  override def enableAsynchronous(): Unit = {
    if (asynchronousEnabled.compareAndSet(false, true)) {
      _asynchronous = new AsyncLoggerSupport(super.log)
    }
  }


  override def flush(): Unit = {
    if (_asynchronous != null)
      _asynchronous.flush()
  }

  override protected[scribe] def log(record: LogRecord): Unit = {
    if (_asynchronous == null) {
      super.log(record)
    } else {
      _asynchronous.log(record)
    }
  }

  override def close(): Unit = {
    if (_asynchronous != null) {
      _asynchronous.close()
    }
  }
}
