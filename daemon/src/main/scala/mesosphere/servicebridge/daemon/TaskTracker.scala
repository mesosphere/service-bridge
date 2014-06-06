package mesosphere.servicebridge.daemon

import akka.actor._
import scala.concurrent.duration.DurationLong

import mesosphere.servicebridge.client.MarathonClient
import mesosphere.servicenet.dsl.Doc
import mesosphere.servicenet.dsl.Doc
import scala.Some

class TaskTracker(marathon: MarathonClient) extends Actor with ActorLogging {
  import context._ // needed for executionContext for scheduler

  var scheduledRefreshTask: Option[Cancellable] = None

  val poller = context.actorOf(
    Props(new TaskTrackerPoller(marathon)),
    "task-tracker-poller"
  )

  override def preStart(): Unit = {
    log.debug("Starting")
    scheduledRefreshTask = Some(
      system.scheduler.schedule(
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

  def receive = {
    case Refresh => poller.forward(Refresh)
  }
}

class TaskTrackerPoller(marathon: MarathonClient)
    extends Actor with ActorLogging with Poller {
  override def serviceName: String = "Marathon"

  def receive = {
    case Refresh =>
      tryOnFuture(marathon.getAppTasks) {
        case runningTasks =>
          log.debug("tasks = {}", runningTasks)
      }
  }
}
