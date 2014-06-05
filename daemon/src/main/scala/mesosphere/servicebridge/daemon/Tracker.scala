package mesosphere.servicebridge.daemon

import akka.actor.{ActorLogging, Actor}
import com.twitter.finagle.{ChannelWriteException, FailedFastException}
import com.twitter.util.{Await, Future}
import java.net.ConnectException
import scala.util.{Failure, Success, Try}

trait Tracker { val me: Actor with ActorLogging

  def tryOnFuture[T](future: Future[T])(f: T => Unit) = {
    Try { Await.result(future) } match {
      case Success(res) => f(res)
      case Failure(_: FailedFastException) => logWarning()
      case Failure(e: ChannelWriteException) =>
        e.getCause match {
          case ce: ConnectException => logWarning()
          case _ => logError(e)
        }
      case Failure(t: Throwable) => logError(t)
    }
  }

  def serviceName: String = ""

  def logWarning() = {
    me.log.warning(s"$serviceName unavailable")
  }

  def logError(t: Throwable) = {
    me.log.error(t, s"Unhandled error communicating with $serviceName")
  }

}
