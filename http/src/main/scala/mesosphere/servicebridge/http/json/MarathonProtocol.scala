package mesosphere.servicebridge.http.json

import mesosphere.servicebridge.http.{
  MarathonTask,
  MesosStatusUpdateEvent,
  TaskData
  TaskHealth
}

import play.api.libs.json._
import play.api.libs.functional.syntax._

/**
  * Custom JSON (de)serializers for Marathon events.
  */
trait MarathonProtocol {

  implicit val mesosStatusUpdateEventFormat =
    Json.format[MesosStatusUpdateEvent]

  implicit val taskHealthFormat =
    Json.format[TaskHealth]

  implicit val marathonTaskFormat =
    Json.format[MarathonTask]

  implicit val taskDataFormat =
    Json.format[TaskData]

}

/**
  * Custom JSON (de)serializers for Marathon events.
  */
object MarathonProtocol extends MarathonProtocol
