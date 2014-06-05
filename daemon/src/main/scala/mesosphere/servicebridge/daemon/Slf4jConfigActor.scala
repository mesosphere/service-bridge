package mesosphere.servicebridge.daemon

import akka.actor.{ ActorLogging, Actor }
import org.slf4j.bridge.SLF4JBridgeHandler
import java.util.logging.{Level, LogManager}

class Slf4jConfigActor extends Actor with ActorLogging {

  override def preStart(): Unit = {
    // Turn off Java util logging so that slf4j can configure it
    LogManager.getLogManager.getLogger("").getHandlers.toList.map { l =>
      l.setLevel(Level.OFF)
    }
    SLF4JBridgeHandler.install()
  }

  override def postStop(): Unit = {
    SLF4JBridgeHandler.uninstall()
  }

  def receive = {
    case msg =>
  }
}
