package mesosphere.servicebridge.daemon

import akka.actor._
import com.twitter.util.Await
import mesosphere.servicebridge.client.MesosClient
import scala.concurrent.duration.DurationDouble
import scala.Some

case object ReloadHosts

class HostTracker(mesos: MesosClient) extends Actor with ActorLogging {
  import context._ // needed for executionContext for scheduler

  var hosts: Map[String, ActorRef] = Map()
  var scheduledRefreshTask: Option[Cancellable] = None

  override def preStart(): Unit = {
    scheduledRefreshTask = Some(
      context.system.scheduler.schedule(
        5.milliseconds,
        10000.milliseconds,
        self,
        Refresh
      )
    )
  }

  override def postStop(): Unit = {
    scheduledRefreshTask.map { _.cancel() }
  }

  def receive = {
    case Refresh =>
      val clusterMembers = Await.result(mesos.getMesosClusterMembers).toSet
      val hostsCopy = hosts

      val toCreate = clusterMembers.filterNot { hostsCopy.keySet.contains }
      val toRemove = hostsCopy.keySet.filterNot { clusterMembers.contains }

      val cleaned = toRemove.foldLeft(hostsCopy) {
        (trackedHosts, hostname) =>
          context.stop(trackedHosts(hostname))
          trackedHosts - hostname
      }

      val cleanedAndAdded = toCreate.foldLeft(cleaned) {
        (trackedHosts, hostname) =>
          val newTrackedHost = hostname ->
            context.system.actorOf(
              Props(new ServiceNetDocPublisher(hostname)),
              s"service-net-doc-publisher-$hostname"
            )
          trackedHosts + newTrackedHost
      }

      log.info("hosts = {}", clusterMembers)
      hosts = cleanedAndAdded
    case p: PublishDoc =>
      hosts.foreach {
        case (hostname, actor) =>
          actor.forward(p)
      }
  }
}
