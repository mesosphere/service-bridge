package mesosphere.servicebridge.http.json

import mesosphere.servicebridge.http.{
  MarathonTask,
  MesosStatusUpdateEvent,
  TaskData,
  TaskHealth
}

import mesosphere.servicenet.util.Spec
import play.api.libs.json._
import play.api.libs.functional.syntax._

class MarathonProtocolSpec extends Spec {

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

    val taskA = MarathonTask(
      id = "service-a_1234",
      appId = "service-a",
      host = "a.corp.org",
      ports = Seq(9001, 9002, 9003),
      healthCheckResults = Some(Seq(
        TaskHealth(alive = true),
        TaskHealth(alive = true)
      ))
    )

    val taskB = MarathonTask(
      id = "service-a_1235",
      appId = "service-a",
      host = "b.corp.org",
      ports = Seq(9004, 9005),
      healthCheckResults = Some(Seq(
        TaskHealth(alive = false),
        TaskHealth(alive = true)
      ))
    )
  }

  import MarathonProtocol._

  "MarathonEventProtocol" should "read and write MesosStatusUpdateEvent" in {
    import Fixture._

    val json = Json.toJson(event)

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

  it should "read and write sequences of MarathonTask instances" in {
    import Fixture._

    val events = Seq(taskA, taskB)
    val json = Json.toJson(events)
    json should equal (
      JsArray(Seq(
        Json.obj(
          "id" -> JsString("service-a_1234"),
          "appId" -> JsString("service-a"),
          "host" -> JsString("a.corp.org"),
          "ports" -> Json.toJson(Seq(9001, 9002, 9003)),
          "healthCheckResults" -> JsArray(Seq(
            Json.obj("alive" -> true),
            Json.obj("alive" -> true)
          ))
        ),
        Json.obj(
          "id" -> JsString("service-a_1235"),
          "appId" -> JsString("service-a"),
          "host" -> JsString("b.corp.org"),
          "ports" -> Json.toJson(Seq(9004, 9005)),
          "healthCheckResults" -> JsArray(Seq(
            Json.obj("alive" -> false),
            Json.obj("alive" -> true)
          ))
        )
      ))
    )

    val readResult = json.as[Seq[MarathonTask]]
    readResult should equal (events)

    intercept[JsResultException] {
      JsString("one fish two fish").as[Seq[MarathonTask]]
    }
  }

  it should "read TaskData from real Marathon output" in {

    val rawJson = """
      {
          "tasks": [
              {
                  "appId": "web",
                  "healthCheckResults": [
                      {
                          "alive": false,
                          "consecutiveFailures": 2,
                          "firstSuccess": null,
                          "lastFailure": "2014-06-02T20:02:21.176Z",
                          "lastFailureCause": "ConnectionAttemptFailedException",
                          "lastSuccess": null,
                          "taskId": "web_1-1401330824836"
                      }
                  ],
                  "host": "localhost",
                  "id": "web_1-1401330824836",
                  "ports": [
                      31972
                  ],
                  "stagedAt": "2014-05-29T02:33:44.837Z",
                  "startedAt": "2014-05-29T02:33:45.550Z",
                  "version": "2014-05-29T02:32:45.172Z"
              },
              {
                  "appId": "web",
                  "healthCheckResults": [
                      {
                          "alive": false,
                          "consecutiveFailures": 2,
                          "firstSuccess": null,
                          "lastFailure": "2014-06-02T20:02:21.174Z",
                          "lastFailureCause": "ConnectionAttemptFailedException",
                          "lastSuccess": null,
                          "taskId": "web_0-1401330548949"
                      }
                  ],
                  "host": "localhost",
                  "id": "web_0-1401330548949",
                  "ports": [
                      31808
                  ],
                  "stagedAt": "2014-05-29T02:29:08.950Z",
                  "startedAt": "2014-05-29T02:29:09.746Z",
                  "version": "2014-05-29T02:29:06.980Z"
              }
          ]
      }
    """

    val json = Json.parse(rawJson)
    val readResult = json.validate[TaskData]
    readResult.get.tasks should have size 2
  }

  it should "read TaskData Marathon output with no healthCheckResults" in {

    val rawJson = """
      {
          "tasks": [
              {
                  "appId": "web",
                  "host": "localhost",
                  "id": "web_1-1401330824836",
                  "ports": [
                      31972
                  ],
                  "stagedAt": "2014-05-29T02:33:44.837Z",
                  "startedAt": "2014-05-29T02:33:45.550Z",
                  "version": "2014-05-29T02:32:45.172Z"
              }
          ]
      }
    """

    val json = Json.parse(rawJson)
    val readResult = json.validate[TaskData]
    readResult.get.tasks should have size 1
  }

}
