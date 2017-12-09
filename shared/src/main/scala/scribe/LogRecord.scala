package scribe

class LogRecord private() {
  private var _level: Level = _
  private var _value: Double = _
  private var _messageFunction: () => Any = _
  private var _className: String = _
  private var _methodName: Option[String] = None
  private var _lineNumber: Int = _
  private var _threadId: Long = _
  private var _threadName: String = _
  private var _timestamp: Long = _
  private var _stringify: Any => String = _

  private var _messageObject: Option[Any] = None
  private var _message: Option[String] = None
  @transient private var _reserved: Boolean = false

  def level: Level = _level
  def value: Double = _value
  def message: String = _message match {
    case Some(m) => m
    case None => {
      val m = _stringify(messageObject)
      _message = Option(m)
      m
    }
  }
  def messageObject: Any = _messageObject match {
    case Some(o) => o
    case None => {
      val o = _messageFunction()
      _messageObject = Option(o)
      o
    }
  }
  def className: String = _className
  def methodName: Option[String] = _methodName
  def lineNumber: Int = _lineNumber
  def threadId: Long = _threadId
  def threadName: String = _threadName
  def timestamp: Long = _timestamp

  def updateValue(value: Double): LogRecord = {
    _value = value
    this
  }

  def release(): Unit = {
    _reserved = false
  }

  override def toString: String = {
    val list = List(
      "level" -> level.name,
      "value" -> value.toString,
      "class" -> className,
      "method" -> methodName.getOrElse("???"),
      "line" -> lineNumber.toString,
      "message" -> message
    )
    list.map {
      case (key, value) => s"$key: $value"
    }.mkString("LogRecord(", ", ", ")")
  }
}

object LogRecord {
  val DefaultStringify: Any => String = (v: Any) => String.valueOf(v)

  private val instance = new ThreadLocal[LogRecord]()

  def apply(level: Level,
            value: Double,
            message: () => Any,
            className: String,
            methodName: Option[String],
            lineNumber: Int,
            threadId: Long = Thread.currentThread().getId,
            threadName: String = Thread.currentThread().getName,
            timestamp: Long = System.currentTimeMillis(),
            stringify: Any => String = DefaultStringify): LogRecord = {
    var r = instance.get()

    val initThreadLocal = r == null

    if (initThreadLocal || r._reserved) {
      r = new LogRecord()
      r._threadId = Thread.currentThread.getId
      r._threadName = Thread.currentThread.getName
      if (initThreadLocal) {
        instance.set(r)
      }
    }

    r._reserved = true

    r._level = level
    r._value = value
    r._messageFunction = message
    r._className = className
    r._methodName = methodName
    r._lineNumber = lineNumber
    r._threadId = threadId
    r._threadName = threadName
    r._timestamp = timestamp
    r._message = None
    r._messageObject = None
    r._stringify = stringify
    r
  }
}