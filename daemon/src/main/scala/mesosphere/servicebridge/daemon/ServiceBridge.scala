package mesosphere.servicebridge.daemon

import mesosphere.servicebridge.http.{
  HTTPServer,
  MarathonTask,
  MesosStatusUpdateEvent,
  TaskData,
  TaskHealth
}
import mesosphere.servicebridge.config.Config

import mesosphere.servicenet.dsl._
import mesosphere.servicenet.util.Logging

object ServiceBridge extends App with Logging {
  implicit val config = Config()

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

  http.run()
}
