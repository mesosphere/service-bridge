package mesosphere.servicebridge.http.json

import mesosphere.servicebridge.http.{ MesosState, Slave }

import mesosphere.servicenet.util.Spec
import play.api.libs.json._
import play.api.libs.functional.syntax._

class MesosProtocolSpec extends Spec {

  object Fixture {
    val state = MesosState(
      slaves = Seq(
        Slave("a.corp.org"),
        Slave("b.corp.org")
      )
    )
  }

  import MesosProtocol._

  "MesosProtocol" should "read and write MesosStatusUpdateEvent" in {
    import Fixture._

    val json = Json.toJson(state)

    json should equal (
      Json.obj(
        "slaves" -> JsArray(Seq(
          Json.obj(
            "hostname" -> JsString("a.corp.org")
          ),
          Json.obj(
            "hostname" -> JsString("b.corp.org")
          )
        ))
      )
    )

    val readResult = json.as[MesosState]
    readResult should equal (state)

    intercept[JsResultException] {
      JsString("one fish two fish").as[MesosState]
    }
  }

  it should "read MesosState from real Mesos output" in {

    val rawJson = """
      {
          "activated_slaves": 1, 
          "build_date": "2014-04-02 10:15:52", 
          "build_time": 1396458952, 
          "build_user": "mesosphere", 
          "completed_frameworks": [], 
          "deactivated_slaves": 0, 
          "failed_tasks": 0, 
          "finished_tasks": 0, 
          "flags": {
              "allocation_interval": "1secs", 
              "authenticate": "false", 
              "framework_sorter": "drf", 
              "help": "false", 
              "logbufsecs": "0", 
              "port": "5050", 
              "quiet": "false", 
              "registry": "local", 
              "root_submissions": "true", 
              "user_sorter": "drf", 
              "webui_dir": "/usr/local/share/mesos/webui", 
              "whitelist": "*", 
              "work_dir": "/tmp/mesos", 
              "zk": "zk://localhost:2181/mesos"
          }, 
          "frameworks": [], 
          "git_sha": "185dba5d8d52034ac6a8e29c2686f0f7dc4cf102", 
          "git_tag": "0.18.0-rc6", 
          "hostname": "localhost", 
          "id": "20140602-161913-16777343-5050-93890", 
          "killed_tasks": 0, 
          "leader": "master@127.0.0.1:5050", 
          "lost_tasks": 0, 
          "pid": "master@127.0.0.1:5050", 
          "slaves": [
              {
                  "attributes": {}, 
                  "hostname": "localhost", 
                  "id": "20140602-161913-16777343-5050-93890-0", 
                  "pid": "slave(1)@127.0.0.1:5051", 
                  "registered_time": 1401751173.24161, 
                  "resources": {
                      "cpus": 4, 
                      "disk": 233112, 
                      "mem": 15360, 
                      "ports": "[31000-32000]"
                  }
              }
          ], 
          "staged_tasks": 0, 
          "start_time": 1401751153.56512, 
          "started_tasks": 0, 
          "version": "0.18.0"
      }
    """

    val json = Json.parse(rawJson)
    val readResult = json.validate[MesosState]
    readResult.get.slaves should have size (1)
  }

}
