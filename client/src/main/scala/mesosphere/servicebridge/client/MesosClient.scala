package mesosphere.servicebridge.client

import com.twitter.util.Future
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.jboss.netty.util.CharsetUtil
import play.api.libs.json.Json

import mesosphere.servicebridge.http.MesosState
import mesosphere.servicebridge.http.json.MesosProtocol
import mesosphere.servicenet.util.Logging

class MesosClient(servers: String)
    extends HttpService with MesosProtocol with Logging {

  val hostAndPort = servers.split(',').head
  lazy val client = new HttpServiceClient(servers)

  def getMesosClusterMembers: Future[Seq[String]] = {
    state() flatMap {
      case opt =>
        opt match {
          case Some(state) => Future.value(state.slaves.map(_.hostname))
          case None        => Future.value(Seq())
        }
    }
  }

  private[client] def state(): Future[Option[MesosState]] = {
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
    }
  }
}
