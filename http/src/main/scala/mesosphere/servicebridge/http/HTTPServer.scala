package mesosphere.servicebridge.http

import mesosphere.servicebridge.config.Config
import mesosphere.servicebridge.http.json.MarathonEventProtocol

import mesosphere.servicenet.dsl._
import mesosphere.servicenet.http.json.DocProtocol
import mesosphere.servicenet.util.Logging
import play.api.libs.json._
import unfiltered.jetty.Http
import unfiltered.request._
import unfiltered.response._

import java.io.File

class HTTPServer(implicit val config: Config = Config())
    extends MarathonEventProtocol
    with DocProtocol
    with Logging {

  def updateToDiff(event: MesosStatusUpdateEvent): Seq[Diff] = {
    ??? // TODO: Generate diffs
  }

  object RestRoutes extends unfiltered.filter.Plan {
    def intent = {
      case req @ Path(Seg("bridge" :: Nil)) => req match {
        case POST(_) =>
          val requestJson = Json.parse(Body.bytes(req))
          requestJson.validate[MesosStatusUpdateEvent] match {
            case JsSuccess(e, path) if e.eventType == "status_update_event" =>
              val eventJson = Json.toJson(e).toString
              log.info(s"Received status update event [$eventJson]")
              // TODO: patch the servicenet doc with the event data
              ResponseHeader("Content-Type", Set("application/json")) ~>
                ResponseString(eventJson)

            case error: JsError =>
              log.info(s"Ignoring event [${requestJson.toString}]")
              ResponseHeader("Content-Type", Set("application/json")) ~>
                ResponseString(JsError.toFlatJson(error).toString)
          }

        case _ => BadRequest ~> ResponseString("Must be PUT")
      }

      case _ => NotFound ~> ResponseString("Not found")
    }
  }

  def run(port: Int = config.httpPort) {
    Http(port).filter(RestRoutes).run
  }
}

object HTTPServer extends App {
  (new HTTPServer).run()
}
