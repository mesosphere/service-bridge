package mesosphere.servicebridge.daemon

import akka.actor._
import com.twitter.util.Future
import scala.Some
import scala.concurrent.duration.DurationDouble

import mesosphere.servicebridge.client.MesosClient

case class TrackHost(hostname: String)
case class UntrackHost(hostname: String)

class HostTracker(mesos: MesosClient) extends Actor with ActorLogging {
  import context._ // needed for executionContext for scheduler

  var hosts: Map[String, ActorRef] = Map()
  var scheduledRefreshTask: Option[Cancellable] = None

  val poller = context.actorOf(
    Props(new HostTrackerPoller(mesos)),
    "host-tracker-poller"
  )

  override def preStart(): Unit = {
    scheduledRefreshTask = Some(
      system.scheduler.schedule(
        10000.milliseconds,
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
    case Refresh => poller ! RefreshHosts(hosts.keySet)
    case p: PublishDoc =>
      hosts.foreach {
        case (hostname, actor) =>
          actor.forward(p)
      }
    case TrackHost(hostname) =>
      hosts += hostname ->
        context.actorOf(
          Props(new ServiceNetDocPublisher(hostname)),
          s"service-net-doc-publisher-$hostname"
        )
    case UntrackHost(hostname) =>
      hosts.get(hostname) match {
        case Some(actorRef) =>
          context.stop(actorRef)
          hosts -= hostname
        case None => // no-o not host tracked by that name
      }
  }
}

case class RefreshHosts(currentHosts: Set[String])
class HostTrackerPoller(mesos: MesosClient) extends Actor with ActorLogging with Poller {
  override def serviceName: String = "Mesos"

  def receive = {
    case RefreshHosts(hosts) =>
      val f = Future.join(
        mesos.getMesosClusterMembers,
        Future.value(hosts)
      ) flatMap {
          case (clusterMembers, trackedHosts) =>
            val toCreate = clusterMembers.filterNot { trackedHosts.contains }
            val toRemove = trackedHosts.filterNot { clusterMembers.contains }
            Future.value(toCreate, toRemove)
        }

      tryOnFuture(f) {
        case (toCreate, toRemove) =>
          toCreate.foreach { sender ! TrackHost(_) }
          toRemove.foreach { sender ! UntrackHost(_) }
      }
      log.debug("hosts = {}", hosts)
  }
}
