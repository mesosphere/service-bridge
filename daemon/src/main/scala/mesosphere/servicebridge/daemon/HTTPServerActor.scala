package mesosphere.servicebridge.daemon

import akka.actor.{ ActorRef, Actor, ActorLogging }
import mesosphere.servicebridge.http.{ MesosStatusUpdateEvent, HTTPServer }
import mesosphere.servicebridge.config.Config

class HTTPServerActor(marathonEventSubscriptionActor: ActorRef)(
  implicit val config: Config = Config())
    extends Actor with ActorLogging {

  val server = new HTTPServer(onMarathonEvent)

  override def preStart(): Unit = {
    server.start()
  }

  override def postStop(): Unit = {
    server.stop()
  }

  def receive = {
    case Refresh =>
  }

  def onMarathonEvent(e: MesosStatusUpdateEvent): Unit = {
    marathonEventSubscriptionActor ! e
  }
}
