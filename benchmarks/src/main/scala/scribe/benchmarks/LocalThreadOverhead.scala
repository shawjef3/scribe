package scribe.benchmarks

import java.util.concurrent.TimeUnit
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.config.Configurator
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory
import org.openjdk.jmh.annotations._
import scribe.Logger

@State(Scope.Thread)
class LocalThreadOverhead {

  scribe.clearHandlers()

  Configurator.initialize(ConfigurationBuilderFactory.newConfigurationBuilder().build())

  val log4jLogger = LogManager.getLogger("test")

  val scribeLogger = Logger.byName("test")
  scribeLogger.clearHandlers()
  scribeLogger.parent.foreach(_.clearHandlers())

  val durationMillis: Long = 5000L

  def logWithScribe(until: Long, message: => Any): Unit = {
    while (System.currentTimeMillis() < until) {
      scribeLogger.info(message)
    }
  }

  def logWithLog4j(until: Long, message: String): Unit = {
    while (System.currentTimeMillis() < until) {
      log4jLogger.info(message)
    }
  }

  /**
    * Subtract the time of this benchmark from the other benchmarks.
    */
  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @OperationsPerInvocation(1000)
  def baseLine(): Int = {
    var i = 0
    while (i < 1000) {
      i += 1
    }
    i
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @OperationsPerInvocation(1000)
  def withScribe(): Unit = {
    var i = 0
    while (i < 1000) {
      scribeLogger.info("test")
      i += 1
    }
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @OperationsPerInvocation(1000)
  def withLog4j(): Unit = {
    var i = 0
    while (i < 1000) {
      log4jLogger.info("test")
      i += 1
    }
  }

}
