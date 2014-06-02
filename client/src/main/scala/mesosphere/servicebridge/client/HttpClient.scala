package mesosphere.servicebridge.client

import mesosphere.servicebridge.config.Config

import dispatch._
import dispatch.Defaults._

class HttpClient(implicit val config: Config = Config()) {

  object Marathon extends HttpService {
    // TODO: get these from config
    val host: String = "marathon.mycorp.net"
    val port: Int = 8080

    val eventSubscribers = url(s"${baseUrl()}/v2/eventSubscribers")
    val tasks = url(s"${baseUrl()}/v2/tasks")
  }

  object ServiceNet extends HttpService {
    // TODO: get these from config
    val host: String = "svcnet.mycorp.net"
    val port: Int = 8080

    val doc = url(s"${baseUrl()}/doc")
  }

  /**
    * Returns a map of app names to tasks
    */
  def getAppTasks(): Map[String, Task] = ???

}
