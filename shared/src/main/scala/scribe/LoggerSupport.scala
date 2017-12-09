package scribe

import scala.language.experimental.macros

trait LoggerSupport {
  def multiplier: Double

  /**
    * Trace log entry. Uses Macros to optimize performance.
    */
   def trace(message: => Any): Unit = macro Macros.trace

   def trace(t: => Throwable): Unit = macro Macros.traceThrowable

  /**
    * Debug log entry. Uses Macros to optimize performance.
    */
   def debug(message: => Any): Unit = macro Macros.debug

   def debug(t: => Throwable): Unit = macro Macros.debugThrowable

  /**
    * Info log entry. Uses Macros to optimize performance.
    */
   def info(message: => Any): Unit = macro Macros.info

   def info(t: => Throwable): Unit = macro Macros.infoThrowable

  /**
    * Warn log entry. Uses Macros to optimize performance.
    */
   def warn(message: => Any): Unit = macro Macros.warn

   def warn(t: => Throwable): Unit = macro Macros.warnThrowable

  /**
    * Error log entry. Uses Macros to optimize performance.
    */
   def error(message: => Any): Unit = macro Macros.error

  /**
    * Error log entry. Uses Macros to optimize performance.
    */
   def error(t: => Throwable): Unit = macro Macros.errorThrowable

  def log(level: Level,
    message: => Any,
    className: String,
    methodName: Option[String] = None,
    lineNumber: Int = -1,
    stringify: Any => String = LogRecord.DefaultStringify): Unit = {
    val record = LogRecord(level, level.value * multiplier, () => message, className, methodName, lineNumber, stringify = stringify)
    log(record)
  }

  protected[scribe] def log(record: LogRecord): Unit

  def accepts(value: Double): Boolean

  def addHandler(handler: LogHandler): Unit

  def removeHandler(handler: LogHandler): Unit

  def clearHandlers(): Unit
}
