package mesosphere.servicebridge.daemon

import akka.actor.{ Cancellable, ActorSystem }
import scala.concurrent.duration.FiniteDuration
import mesosphere.servicenet.util.Logging

sealed class PeriodicTaskScheduler extends Logging {
  private[daemon] val system = ActorSystem("Scheduler")
  private implicit val execContext = system.dispatcher
  private val scheduler = system.scheduler

  /**
    * Builder function to register a system shutdown hook for this
    * `PeriodicTaskScheduler`
    * @return `this` `PeriodicTaskScheduler` after having registered a system
    *        shutdown hook.
    */
  def releaseOnShutdown(): PeriodicTaskScheduler = {
    log.debug("Registering shutdown hook for PeriodicTaskScheduler ActorSystem")
    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run() {
        log.debug("Shutting down PeriodicTaskScheduler ActorSystem")
        system.shutdown()
      }
    })
    this
  }

  /**
    * Schedule a task to run at the specified `interval`. The first invocation
    * of `f` will be schedule to start in `interval` time.
    * @param interval The initialDelay and interval to run `f`
    * @param f The closure to run periodically
    * @return Returns a `Cancellable` that allows for the client to cancel a task
    *         as well as test if the task has been canceled.
    */
  def schedule(interval: FiniteDuration)(f: => Unit): Cancellable =
    schedule(interval, interval)(f)

  /**
    * Schedule a task to run at the specified `interval` following the first
    * invocation which will happend after `initialDelay`.
    * @param initialDelay The delay before running `f` for the first time
    * @param interval The interval to run `f`
    * @param f The closure to run periodically
    * @return Returns a `Cancellable` that allows for the client to cancel a task
    *         as well as test if the task has been canceled.
    */
  def schedule(initialDelay: FiniteDuration, interval: FiniteDuration)(f: => Unit): Cancellable =
    scheduler.schedule(initialDelay, interval)(f)

  /**
    * Schedule a task to run after some `delay`
    * @param delay The time to wait before running `f`
    * @param f The closure to run after `delay`
    * @return Returns a `Cancellable` that allows for the client to cancel a task
    *         as well as test if the task has been canceled.
    */
  def scheduleOnce(delay: FiniteDuration)(f: => Unit): Cancellable =
    scheduler.scheduleOnce(delay)(f)
}

object PeriodicTaskScheduler extends Logging {
  private lazy val scheduler = new PeriodicTaskScheduler().releaseOnShutdown()
  def apply(): PeriodicTaskScheduler = {
    scheduler
  }
}
