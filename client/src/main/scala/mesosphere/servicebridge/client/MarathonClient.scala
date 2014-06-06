package mesosphere.servicebridge.client

import com.github.theon.uri.Uri._
import com.twitter.util.Future
import java.net.URL
import org.jboss.netty.handler.codec.http.HttpResponse
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.jboss.netty.util.CharsetUtil
import play.api.libs.json.Json

import mesosphere.servicebridge.http.json.MarathonProtocol
import mesosphere.servicebridge.http.{ MarathonTask, TaskData }
import mesosphere.servicenet.util.Logging

class MarathonClient(servers: String)
    extends HttpService with MarathonProtocol with Logging {

  val hostAndPort = servers.split(',').head
  lazy val client = new HttpServiceClient(servers)

  /**
    * Returns a mapping from app names to tasks
    */
  def getAppTasks: Future[Map[String, Seq[MarathonTask]]] = {
    tasks() flatMap {
      case opt =>
        opt match {
          case Some(taskData) =>
            val group = taskData.tasks.groupBy(_.appId)
            Future.value(group)
          case None => Future.value(Map())
        }
    }
  }

  def subscribeToEvents(callbackUrl: URL) = {
    val r = post(hostAndPort)("/v2/eventSubscriptions" ? ("callbackUrl" -> callbackUrl))()
    client(r) flatMap subscriptionFilter
  }

  def unsubscribeFromEvents(callbackUrl: URL) = {
    val r = delete(hostAndPort)("/v2/eventSubscriptions" ? ("callbackUrl" -> callbackUrl))
    client(r) flatMap subscriptionFilter
  }

  private[client] def tasks() = {
    val r = get(hostAndPort)("/v2/tasks")
    client(r) flatMap {
      // TODO: Increment stats for these requests
      case response =>
        response.getStatus match {
          case OK =>
            val content = response.getContent.toString(CharsetUtil.UTF_8)
            val json = Json.parse(content)
            val tasks = json.validate[TaskData]
            val opt = tasks.asOpt
            Future.value(opt)
          case _ => Future.value(None)
        }
    }
  }

  private[this] def subscriptionFilter(response: HttpResponse): Future[String] = {
    response.getStatus match {
      case OK => Future.value("Ok")
      case _ =>
        val status = response.getStatus
        Future.exception(
          new Exception(s"${status.getCode} ${status.getReasonPhrase}")
        )
    }
  }

}
