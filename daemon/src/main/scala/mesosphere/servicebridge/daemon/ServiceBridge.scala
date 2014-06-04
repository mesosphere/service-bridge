package mesosphere.servicebridge.daemon

import akka.actor.{ Props, ActorSystem }
import mesosphere.servicebridge.config.Config
import mesosphere.servicebridge.client.{ MesosClient, MarathonClient }

object ServiceBridge extends App {
  implicit val config = Config()

  val system = ActorSystem("service-bridge")
  //  system.logConfiguration()

  val marathonClient = new MarathonClient(config.marathon)
  val mesosClient = new MesosClient(config.mesos)

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

  Runtime.getRuntime.addShutdownHook(new Thread() {
    override def run() = system.shutdown()
  })

  system.awaitTermination()
}
