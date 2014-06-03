package mesosphere.servicebridge.http

case class Slave(hostname: String)

case class MesosState(slaves: Seq[Slave])
