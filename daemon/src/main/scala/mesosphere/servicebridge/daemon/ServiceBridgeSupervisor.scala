package mesosphere.servicebridge.daemon

import akka.actor.{Props, Actor, ActorLogging}
import mesosphere.servicebridge.client.{MesosClient, MarathonClient}
import mesosphere.servicebridge.config.Config

class ServiceBridgeSupervisor(implicit config: Config = Config())
  extends Actor
          with ActorLogging {

  val marathonClient = new MarathonClient(config.marathon)
  val mesosClient = new MesosClient(config.mesos)

  override def preStart(): Unit = {
    val marathonActor = context.actorOf(
      Props(new MarathonEventSubscriptionActor(marathonClient)),
      "marathon-event-subscription"
    )

    val httpServer = context.actorOf(
      Props(new HTTPServerActor(marathonActor)),
      "http-server"
    )

    val hostTracker = context.actorOf(
      Props(new HostTracker(mesosClient)),
      "host-tracker"
    )

    val taskTracker = context.actorOf(
      Props(new TaskTracker(marathonClient, hostTracker)),
      "task-tracker"
    )
  }

  def receive = {
    case msg =>
  }

}
