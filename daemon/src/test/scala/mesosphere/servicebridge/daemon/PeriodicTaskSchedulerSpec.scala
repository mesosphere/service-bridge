package mesosphere.servicebridge.daemon

import mesosphere.servicenet.util.Spec
import scala.concurrent.duration.DurationDouble
import akka.testkit.TestProbe

class PeriodicTaskSchedulerSpec extends Spec {

  val scheduler = PeriodicTaskScheduler()
  implicit val system = scheduler.system

  "PeriodicTaskScheduler" should "Return the same instance for multiple calls" in {
    assert(scheduler === PeriodicTaskScheduler())
  }

  it should "schedule a task" in {
    var counter = 0
    val task = scheduler.schedule(1.millisecond){ counter += 1 }
    TestProbe().awaitCond(
      { counter > 0 },
      100.milliseconds,
      message = "Timeout while waiting for counter to increment"
    )
    task.cancel()
    assert(counter > 0)
    assert(task.isCancelled)
  }

  it should "schedule a single run of a task" in {
    var counter = 0
    scheduler.scheduleOnce(1.millisecond) { counter += 1 }
    TestProbe().awaitCond(
      { counter > 0 },
      100.milliseconds,
      message = "Timeout while waiting for counter to be set to 1"
    )
    assert(counter === 1)
  }

}
