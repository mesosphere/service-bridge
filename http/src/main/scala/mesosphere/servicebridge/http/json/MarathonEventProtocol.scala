package mesosphere.servicebridge.http.json

import mesosphere.servicebridge.http.MesosStatusUpdateEvent

import play.api.libs.json._
import play.api.libs.functional.syntax._

/**
  * Custom JSON (de)serializers for Marathon events.
  */
trait MarathonEventProtocol {

  implicit val mesosStatusUpdateEventFormat =
    Json.format[MesosStatusUpdateEvent]

}

/**
  * Custom JSON (de)serializers for Marathon events.
  */
object MarathonEventProtocol extends MarathonEventProtocol
