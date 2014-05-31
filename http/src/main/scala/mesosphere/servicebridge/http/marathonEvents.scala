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
