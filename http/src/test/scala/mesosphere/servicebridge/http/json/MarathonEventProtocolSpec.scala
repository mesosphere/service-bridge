package mesosphere.servicebridge.http.json

import mesosphere.servicebridge.http.MesosStatusUpdateEvent

import mesosphere.servicenet.util.Spec
import play.api.libs.json._
import play.api.libs.functional.syntax._

class MarathonEventProtocolSpec extends Spec {

  object Fixture {
    val event = MesosStatusUpdateEvent(
      eventType = "status_update_event",
      appId = "app-123",
      taskId = "app-123-task-8675309",
      taskStatus = "TASK_RUNNING",
      host = "foo.ultramega.co",
      slaveId = "abcd1234",
      ports = Seq(9001, 9002, 9003),
      timestamp = "2014-05-29T04:15:50Z"
    )
  }

  import MarathonEventProtocol._

  "MarathonEventProtocol" should "read and write MesosStatusUpdateEvent" in {
    import Fixture._

    val json = Json.toJson(event)

    log.info("json: [{}]", Json.prettyPrint(json))

    json should equal (
      Json.obj(
        "eventType" -> JsString("status_update_event"),
        "appId" -> JsString("app-123"),
        "taskId" -> JsString("app-123-task-8675309"),
        "taskStatus" -> JsString("TASK_RUNNING"),
        "host" -> JsString("foo.ultramega.co"),
        "slaveId" -> JsString("abcd1234"),
        "ports" -> Json.toJson(Seq(9001, 9002, 9003)),
        "timestamp" -> JsString("2014-05-29T04:15:50Z")
      )
    )

    val readResult = json.as[MesosStatusUpdateEvent]
    readResult should equal (event)

    intercept[JsResultException] {
      JsString("one fish two fish").as[MesosStatusUpdateEvent]
    }
  }

}
