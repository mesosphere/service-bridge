package mesosphere.servicebridge.daemon

import akka.actor.{ ActorRef, Cancellable, ActorLogging, Actor }
import scala.concurrent.duration.DurationLong

import mesosphere.servicebridge.client.MarathonClient
import mesosphere.servicenet.dsl.Doc

class TaskTracker(marathon: MarathonClient, hostTracker: ActorRef)
    extends Actor with ActorLogging with Tracker {
  val me = this

  import context._ // needed for executionContext for scheduler

  var scheduledRefreshTask: Option[Cancellable] = None

  override def preStart(): Unit = {
    log.debug("Starting")
    scheduledRefreshTask = Some(
      context.system.scheduler.schedule(
        10000.milliseconds,
        10000.milliseconds,
        self,
        Refresh
      )
    )
  }

  override def postStop(): Unit = {
    scheduledRefreshTask.map { _.cancel() }
    log.debug("Stopped")
  }

  override def serviceName: String = "Marathon"

  def receive = {
    case Refresh =>
      tryOnFuture(marathon.getAppTasks) {
        case runningTasks =>
          log.debug("tasks = {}", runningTasks)
          hostTracker ! PublishDoc(Doc())
      }
  }
}
