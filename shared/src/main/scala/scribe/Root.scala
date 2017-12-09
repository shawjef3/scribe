package scribe

import scala.language.experimental.macros

trait Root extends SynchronousLogger {
  override val name: Option[String] = Some("scribe")
  override val parentName: Option[String] = Some(Logger.rootName)
  override val multiplier: Double = 1.0

  def enableAsynchronous(): Unit
  def flush(): Unit

  implicit class AnyLogging(value: Any) {
    def logger: Logger = Logger.byName(value.getClass.getSimpleName)
  }
}