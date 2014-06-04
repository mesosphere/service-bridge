package mesosphere.servicebridge.http

import mesosphere.servicebridge.config.Config
import mesosphere.servicebridge.http.json.MarathonProtocol

import mesosphere.servicenet.util.Logging
import play.api.libs.json._
import unfiltered.jetty.Http
import unfiltered.request._
import unfiltered.response._

import java.io.File

class HTTPServer(handleEvent: MesosStatusUpdateEvent => Unit = _ => ())(
  implicit val config: Config = Config())
    extends MarathonProtocol
    with Logging {

  private[this] val server = Http(config.httpPort).filter(RestRoutes)

  object RestRoutes extends unfiltered.filter.Plan {
    def intent = {
      case req @ Path(Seg("bridge" :: Nil)) => req match {
        case POST(_) =>
          val requestJson = Json.parse(Body.bytes(req))
          requestJson.validate[MesosStatusUpdateEvent] match {
            case JsSuccess(e, path) if e.eventType == "status_update_event" =>
              val eventJson = Json.toJson(e).toString()
              log.debug(s"Received status update event [$eventJson]")

              // delegate handling of Marathon event
              handleEvent(e)

              ResponseHeader("Content-Type", Set("application/json")) ~>
                ResponseString(eventJson)

            case error: JsError =>
              val errorJson = JsError.toFlatJson(error).toString()
              log.warn(
                s"Ignoring event [${requestJson.toString()}] " +
                  s"Unexpected Json Format: [$errorJson]"
              )
              ResponseHeader("Content-Type", Set("application/json")) ~>
                ResponseString(errorJson)
          }

        case _ => BadRequest ~> ResponseString("Must be POST")
      }

      case _ => NotFound ~> ResponseString("Not found")
    }
  }

  def start() {
    // use start rather than run here because we're managing the lifecycle of
    // the server with akka
    server.start()
  }

  def stop() {
    server.stop()
  }
}
