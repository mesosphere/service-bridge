package mesosphere.servicebridge.client

import mesosphere.servicebridge.config.Config
import mesosphere.servicebridge.http.MarathonTask

import dispatch._
import dispatch.Defaults._
import java.net.URL

class HttpClient(implicit val config: Config = Config()) {

  object Marathon extends HttpService {
    val urls =
      config.marathon.split(',').map { hostAndPort =>
        new URL(s"http://$hostAndPort")
      }
    override def baseUrls: Seq[URL] = urls

    val eventSubscribers = "/v2/eventSubscribers"
    val tasks = "/v2/tasks"
  }

  object Mesos extends HttpService {
    val urls =
      config.mesos.split(',').map { hostAndPort =>
        new URL(s"http://$hostAndPort")
      }
    override def baseUrls: Seq[URL] = urls

    val state = "/state.json"
  }

  object ServiceNet extends HttpService {
    // TODO: get these from MesosState
    override def baseUrls = ???

    val doc = "/doc"
  }

  /**
    * Returns a mapping from app names to tasks
    */
  def getAppTasks(): Map[String, MarathonTask] = {
    // do a GET against Marathon.tasks
    // json.validate[mesosphere.servicebridge.http.TaskData]
    // taskData.tasks.groupBy(_.appId)
    ???
  }

  /**
    * Returns a map of app names to tasks
    */
  def getMesosClusterMembers(): Seq[String] = {
    // do a GET against Mesos.state
    // json.validate[mesosphere.servicebridge.http.MesosState]
    // mesosState.slaves.map(_.hostname)
    ???
  }

}
