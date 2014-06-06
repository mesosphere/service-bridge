package mesosphere.servicebridge.daemon

import akka.actor.{ Actor, ActorLogging }
import mesosphere.servicebridge.config.Config
import mesosphere.servicebridge.http.{ MesosStatusUpdateEvent, HTTPServer }

class HTTPServerActor(
  implicit val config: Config = Config())
    extends Actor with ActorLogging {
  import context._

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
    parent ! e
  }
}
