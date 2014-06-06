package mesosphere.servicebridge.daemon

import akka.actor.{ Props, ActorSystem }
import mesosphere.servicebridge.config.Config

object ServiceBridge extends App {
  implicit val config = Config()

  val system = ActorSystem("service-bridge")
  val slf4jConfigActor = system.actorOf(
    Props(new Slf4jConfigActor)
  )
  //  system.logConfiguration()

  val serviceBridgeSupervisor = system.actorOf(
    Props(new ServiceBridgeSupervisor),
    "service-bridge-supervisor"
  )

  Runtime.getRuntime.addShutdownHook(new Thread() {
    override def run() = system.shutdown()
  })

  system.awaitTermination()
}
