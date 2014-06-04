package mesosphere.servicebridge.daemon

import akka.actor.{ ActorLogging, Actor }

class ServiceNetDocPublisher(hostname: String) extends Actor with ActorLogging {

  override def preStart() = {
    log.info("starting") // TODO: Debug
  }

  override def postStop() = {
    log.info("stopped") // TODO: Debug
  }

  def receive = {
    case PublishDoc(doc) => log.info("doc = {}", doc)
  }

}
