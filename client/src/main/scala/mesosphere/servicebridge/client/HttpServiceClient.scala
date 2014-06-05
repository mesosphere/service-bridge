package mesosphere.servicebridge.client

import org.jboss.netty.handler.codec.http.{HttpResponse, HttpRequest}
import com.twitter.finagle.{FailedFastException, Http, Service}
import com.twitter.util.Future
import mesosphere.servicenet.util.Logging

class HttpServiceClient(servers: String) 
  extends Service[HttpRequest, HttpResponse] with Logging {
  val service = Http.newService(servers)

  override def apply(req: HttpRequest): Future[HttpResponse] = {
    service(req) onFailure {
      case failFast: FailedFastException =>
        log.trace(
          s"Error while processing request: ${req.getMethod} " +
            s"http://${req.headers().get("Host")}${req.getUri}"
        // don't include the exception it doesn't any an info (empty stack)
        )
      case t: Throwable =>
        log.trace(
          s"Error while processing request: ${req.getMethod} " +
            s"http://${req.headers().get("Host")}${req.getUri}",
          t
        )
    }
  }
}
