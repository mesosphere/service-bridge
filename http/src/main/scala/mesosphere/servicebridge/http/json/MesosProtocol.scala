package mesosphere.servicebridge.http.json

import mesosphere.servicebridge.http.{ MesosState, Slave }

import play.api.libs.json._
import play.api.libs.functional.syntax._

/**
  * Custom JSON (de)serializers for Mesos state.
  */
trait MesosProtocol {

  implicit val slaveFormat =
    Json.format[Slave]

  implicit val mesosStateFormat =
    Json.format[MesosState]

}

/**
  * Custom JSON (de)serializers for Mesos state.
  */
object MesosProtocol extends MesosProtocol
