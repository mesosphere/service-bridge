package mesosphere.servicebridge.daemon

import akka.actor.{ ActorRef, Cancellable, ActorLogging, Actor }
import com.twitter.util.Await
import scala.concurrent.duration.DurationLong

import mesosphere.servicenet.dsl.Doc

import mesosphere.servicebridge.client.MarathonClient

case object ReloadTasks

class TaskTracker(marathon: MarathonClient, hostTracker: ActorRef)
    extends Actor with ActorLogging {
  import context._ // needed for executionContext for scheduler

  var scheduledRefreshTask: Option[Cancellable] = None

  override def preStart(): Unit = {
    scheduledRefreshTask = Some(
      context.system.scheduler.schedule(
        5.milliseconds,
        10000.milliseconds,
        self,
        Refresh
      )
    )
  }

  override def postStop(): Unit = {
    scheduledRefreshTask.map { _.cancel() }
  }

  def receive = {
    case Refresh =>
      val runningTasks = Await.result(marathon.getAppTasks)
      log.info("tasks = {}", runningTasks)
      hostTracker ! PublishDoc(Doc())
  }
}
