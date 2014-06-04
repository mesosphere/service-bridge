package mesosphere.servicebridge.daemon

import java.net.{ InetAddress, URL }
import com.twitter.util.Future
import scala.concurrent.duration.DurationInt

import mesosphere.servicenet.dsl._
import mesosphere.servicenet.util.Logging

import mesosphere.servicebridge.client.HttpClient
import mesosphere.servicebridge.config.Config
import mesosphere.servicebridge.http.{
  HTTPServer,
  MesosStatusUpdateEvent,
  TaskData
}

object ServiceBridge extends App with Logging {
  implicit val config = Config()

  lazy val localHostName = InetAddress.getLocalHost.getCanonicalHostName
  val callbackUrl = new URL(s"http://$localHostName:${config.httpPort}/bridge")

  val doc: Doc = Doc()

  /**
    * Calculates a Diff from the supplied Doc and event.
    */
  def diffFromEvent(doc: Doc, event: MesosStatusUpdateEvent): Diff = {
    // TODO: construct a meaningful diff here
    Diff()
  }

  /**
    * Builds a network Doc from the supplied task data.
    */
  def docFromTasks(taskData: TaskData): Doc = {
    // TODO: construct a meaningful doc here
    Doc()
  }

  def handleEvent(event: MesosStatusUpdateEvent): Unit = {
    val diff = diffFromEvent(doc, event)
    log.info(s"Calculated diff: [$diff]")
    // ServiceNetConnectionManager.patchAllWith(diff)
  }

  val http = new HTTPServer(handleEvent)
  val client = new HttpClient()
  val scheduler = PeriodicTaskScheduler()

  client.Marathon.subscribeToEvents(callbackUrl) onSuccess {
    case s =>
      log.debug("Successfully registered event callback with marathon")
      Runtime.getRuntime.addShutdownHook(new Thread() {
        override def run() {
          client.Marathon.unsubscribeFromEvents(callbackUrl) onSuccess {
            case s =>
              log.debug("Successfully unregister event callbacks with marathon")
          } onFailure {
            case t: Throwable =>
              log.debug(
                "Error while trying to unregister event callback with marathon",
                t
              )
          }
        }
      })
  } onFailure {
    case t: Throwable =>
      log.error("Failed to register callback", t)
  }

  val task = scheduler.schedule(5000.milliseconds, 15000.milliseconds) {
    Future.join(
      client.getMesosClusterMembers,
      client.getAppTasks
    ) onSuccess {
        case (clusterMembers, tasks) =>
          log.info("clusterMembers = {}", clusterMembers)
          log.info("tasks = {}", tasks)
      } onFailure {
        case t: Throwable =>
          log.info("I can recover from this", t)
      }
  }
  log.info("task = {}", task)

  http.run()
}
