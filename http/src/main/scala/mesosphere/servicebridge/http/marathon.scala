package mesosphere.servicebridge.http

sealed trait MarathonEvent

case class MesosStatusUpdateEvent(eventType: String,
                                  appId: String,
                                  taskId: String,
                                  taskStatus: String,
                                  host: String,
                                  slaveId: String,
                                  ports: Iterable[Int],
                                  timestamp: String) extends MarathonEvent

case class TaskHealth(alive: Boolean)

case class MarathonTask(id: String,
                        appId: String,
                        host: String,
                        ports: Seq[Int],
                        healthCheckResults: Seq[TaskHealth])

case class TaskData(tasks: Seq[MarathonTask])
