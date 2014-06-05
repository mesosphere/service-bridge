package mesosphere.servicebridge.daemon

import akka.actor.{Props, Actor, ActorLogging}
import mesosphere.servicebridge.client.{MesosClient, MarathonClient}
import mesosphere.servicebridge.config.Config

class ServiceBridgeSupervisor(implicit config: Config = Config())
  extends Actor
          with ActorLogging {

  val marathonClient = new MarathonClient(config.marathon)
  val mesosClient = new MesosClient(config.mesos)
  lazy val system = context.system

  override def preStart(): Unit = {
    val marathonActor = system.actorOf(
      Props(new MarathonEventSubscriptionActor(marathonClient)),
      "marathon-event-subscription"
    )

    val httpServer = system.actorOf(
      Props(new HTTPServerActor(marathonActor)),
      "http-server"
    )

    val hostTracker = system.actorOf(
      Props(new HostTracker(mesosClient)),
      "host-tracker"
    )

    val taskTracker = system.actorOf(
      Props(new TaskTracker(marathonClient, hostTracker)),
      "task-tracker"
    )
  }

  def receive = {
    case msg =>
  }

}
