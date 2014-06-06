package mesosphere.servicebridge.daemon

import akka.actor.{ ActorLogging, Actor }
import com.ning.http.client.Response
import dispatch._
import dispatch.Defaults._
import play.api.libs.json.Json
import mesosphere.servicenet.http.json.DocProtocol

class ServiceNetDocPublisher(hostname: String)
    extends Actor
    with ActorLogging
    with DocProtocol {

  def receive = {
    case PublishDoc(doc) =>
      val jsonString = Json.toJson(doc).toString()
      val putReq = url(s"http://$hostname:9000/doc").PUT
        .setContentType("application/json", "UTF-8") << jsonString
      Http(putReq > { (response: Response) =>
        //TODO: Increment counter with hostname and status code
      })
  }

}
