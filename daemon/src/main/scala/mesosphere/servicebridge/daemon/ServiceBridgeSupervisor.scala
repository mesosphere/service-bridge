package mesosphere.servicebridge.daemon

import akka.actor.{Cancellable, Props, Actor, ActorLogging}
import mesosphere.servicebridge.client.{ MesosClient, MarathonClient }
import mesosphere.servicebridge.config.Config
import mesosphere.servicebridge.http.MesosStatusUpdateEvent
import scala.concurrent.duration.DurationInt
import mesosphere.servicenet.dsl.Doc

class ServiceBridgeSupervisor(implicit config: Config = Config())
    extends Actor
    with ActorLogging {
  import context._

  val marathonClient = new MarathonClient(config.marathon)
  val mesosClient = new MesosClient(config.mesos)

  val marathonActor = context.actorOf(
    Props(new MarathonEventSubscriptionActor(marathonClient)),
    "marathon-event-subscription"
  )

  val httpServer = context.actorOf(
    Props(new HTTPServerActor()),
    "http-server"
  )

  val hostTracker = context.actorOf(
    Props(new HostTracker(mesosClient)),
    "host-tracker"
  )

  val taskTracker = context.actorOf(
    Props(new TaskTracker(marathonClient)),
    "task-tracker"
  )

  var refreshTask: Option[Cancellable] = None
  override def preStart() = {
    log.debug("Starting")
    refreshTask = Some(
      system.scheduler.schedule(
        10000.milliseconds,
        10000.milliseconds,
        self,
        Refresh
      )
    )
  }

  override def postStop() = {
    refreshTask.map { _.cancel() }
    log.debug("Stopped")
  }

  def receive = {
    case Refresh => self ! PublishDoc(Doc())
    case doc: PublishDoc           => hostTracker.forward(doc)
    case e: MesosStatusUpdateEvent => marathonActor.forward(e)
  }
}
