package mesosphere.servicebridge.daemon

import akka.actor.{ ActorLogging, Actor }
import java.net.{ InetAddress, URL }

import mesosphere.servicebridge.client.MarathonClient
import mesosphere.servicebridge.config.Config
import mesosphere.servicebridge.http.MesosStatusUpdateEvent

class MarathonEventSubscriptionActor(marathon: MarathonClient)(
  implicit config: Config = Config())
    extends Actor with ActorLogging {

  lazy val localHostName = InetAddress.getLocalHost.getCanonicalHostName
  val callbackUrl = new URL(s"http://$localHostName:${config.httpPort}/bridge")

  override def preStart(): Unit = {
    marathon.subscribeToEvents(callbackUrl)
  }

  override def postStop(): Unit = {
    marathon.unsubscribeFromEvents(callbackUrl)
  }

  def receive = {
    case e: MesosStatusUpdateEvent => log.info("e = {}", e)
  }
}
