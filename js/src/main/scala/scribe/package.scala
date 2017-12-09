import scala.language.experimental.macros

package object scribe extends Root {
  override def enableAsynchronous(): Unit = ()
}