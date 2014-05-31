package mesosphere.servicebridge.daemon

import mesosphere.servicebridge.http.HTTPServer
import mesosphere.servicebridge.config.Config
import mesosphere.servicenet.util.Logging

object ServiceBridge extends App with Logging {
  implicit val config = Config()
  val http = new HTTPServer
  http.run()
}
