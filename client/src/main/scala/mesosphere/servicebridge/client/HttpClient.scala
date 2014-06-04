package mesosphere.servicebridge.client

import com.github.theon.uri.Uri._
import com.twitter.finagle.Http
import com.twitter.util.Future

import java.net.URL

import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.jboss.netty.util.CharsetUtil

import play.api.libs.json.Json

import mesosphere.servicebridge.config.Config
import mesosphere.servicebridge.http.json.{ MarathonProtocol, MesosProtocol }
import mesosphere.servicebridge.http.{ TaskData, MesosState, MarathonTask }
import mesosphere.servicenet.util.Logging

class HttpClient(implicit val config: Config = Config()) {

  object Marathon extends HttpService with MarathonProtocol with Logging {
    val hostAndPort = config.marathon.split(',').head
    lazy val client = Http.newService(config.marathon)

    def subscribeToEvents(callbackUrl: URL) = {
      client(
        post(hostAndPort)("/v2/eventSubscriptions" ? ("callbackUrl" -> callbackUrl))()
      ) flatMap {
          case response =>
            response.getStatus match {
              case OK => Future.value("Ok")
              case _ =>
                val status = response.getStatus
                Future.exception(
                  new Exception(s"${status.getCode} ${status.getReasonPhrase}")
                )
            }
        } onFailure {
          case t: Throwable =>
            log.debug("Error", t)
        }
    }

    def unsubscribeFromEvents(callbackUrl: URL) = {
      client(
        delete(hostAndPort)("/v2/eventSubscriptions" ? ("callbackUrl" -> callbackUrl))
      ) flatMap {
          case response =>
            response.getStatus match {
              case OK => Future.value("Ok")
              case _ =>
                val status = response.getStatus
                Future.exception(
                  new Exception(s"${status.getCode} ${status.getReasonPhrase}")
                )
            }
        } onFailure {
          case t: Throwable =>
            log.debug("Error", t)
        }
    }

    def tasks() = {
      client(get(hostAndPort)("/v2/tasks")) flatMap {
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
      } onFailure {
        case t: Throwable =>
          log.debug("Error", t)
      }
    }
  }

  object Mesos extends HttpService with MesosProtocol with Logging {
    val hostAndPort = config.mesos.split(',').head
    lazy val client = Http.newService(config.mesos)

    def state() = {
      client(get(hostAndPort)("/master/state.json")) flatMap {
        case response =>
          // TODO: Increment stats for these requests
          response.getStatus match {
            case OK =>
              val content = response.getContent.toString(CharsetUtil.UTF_8)
              val json = Json.parse(content)
              val state = json.validate[MesosState]
              val opt = state.asOpt
              Future.value(opt)
            case _ => Future.value(None)
          }
      } onFailure {
        case t: Throwable =>
          log.debug("Error", t)
      }
    }
  }

  object ServiceNet extends HttpService {
    // TODO: get these from MesosState

    val doc = "/doc"
  }

  /**
    * Returns a mapping from app names to tasks
    */
  def getAppTasks: Future[Map[String, MarathonTask]] = {
    Marathon.tasks() flatMap {
      case opt =>
        opt match {
          case Some(taskData) =>
            val group = taskData.tasks.groupBy(_.appId).map {
              case t: (String, Seq[MarathonTask]) => (t._1, t._2.head)
            }
            Future.value(group)
          case None => Future.value(Map())
        }
    }
  }

  def getMesosClusterMembers: Future[Seq[String]] = {
    Mesos.state() flatMap {
      case opt =>
        opt match {
          case Some(state) => Future.value(state.slaves.map(_.hostname))
          case None        => Future.value(Seq())
        }
    }
  }

}
