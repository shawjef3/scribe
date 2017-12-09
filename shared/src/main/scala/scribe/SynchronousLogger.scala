package scribe

trait SynchronousLogger extends LoggerSupport {
  @volatile private[scribe] var _handlers = Set.empty[LogHandler]
  private[scribe] final val handlersLock = new AnyRef

  private[scribe] def handlers = _handlers

  def name: Option[String]
  def parentName: Option[String]

  def parent: Option[Logger] = parentName.map(Logger.byName)

  /**
    * Log method invoked by trace, debug, info, warn, and error. Ideally should not be called directly as it will not
    * be able to take advantage of Macro optimizations.
    *
    * @param level the logging level
    * @param message function to derive the message for the log
    * @param methodName the method name if applicable
    * @param lineNumber the line number the logging was invoked on
    * @param stringify the conversion function to generate a String from the message.
    *                  Defaults to LogRecord.DefaultStringify.
    */
  override def log(level: Level,
    message: => Any,
    className: String,
    methodName: Option[String] = None,
    lineNumber: Int = -1,
    stringify: Any => String = LogRecord.DefaultStringify): Unit = if (accepts(level.value)) {
    val record = LogRecord(level, level.value * multiplier, () => message, className, methodName, lineNumber, stringify = stringify)
    log(record)
  }

  protected[scribe] def log(record: LogRecord): Unit = {
    handlers.foreach(h => h.log(record))
    parent.foreach(p => p.log(record.updateValue(record.value * p.multiplier)))
    record.release()
  }

  /**
    * Returns true if the supplied value will be accepted by a handler of this logger or an ancestor (up the parent
    * tree)
    */
  override def accepts(value: Double): Boolean = {
    val v = value * multiplier

    handlers.exists(handler => handler.accepts(v)) || parent.exists(p => p.accepts(v))
  }

  /**
    * Adds a handler that will receive log records submitted to this logger and any descendant loggers.
    */
  override def addHandler(handler: LogHandler): Unit = handlersLock.synchronized {
    _handlers += handler
  }

  /**
    * Removes an handler that was previously added to this logger.
    */
  override def removeHandler(handler: LogHandler): Unit = handlersLock.synchronized {
    _handlers -= handler
  }

  /**
    * Removes all handlers currently on this logger.
    */
  override def clearHandlers(): Unit = handlersLock.synchronized {
    _handlers = Set.empty
  }
}
