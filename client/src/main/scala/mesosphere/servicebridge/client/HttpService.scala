package mesosphere.servicebridge.client

import com.github.theon.uri.Uri
import com.twitter.finagle.http.RequestBuilder
import org.jboss.netty.buffer.ChannelBuffers
import mesosphere.servicenet.util.Logging

trait HttpService extends Logging {

  def uri2url(hostAndPort: String, uri: Uri): String = {
    s"http://$hostAndPort/${uri.toString()}".replaceAll("(?<!:)//", "/")
  }

  def get(hostAndPort: String)(uri: Uri) = {
    RequestBuilder()
      .url(uri2url(hostAndPort, uri))
      .buildGet()
  }

  def post(hostAndPort: String)(uri: Uri)(postBody: Array[Byte] = Array()) = {
    RequestBuilder()
      .url(uri2url(hostAndPort, uri))
      .addHeader("Content-Length", s"${postBody.length}")
      .buildPost(ChannelBuffers.wrappedBuffer(postBody))
  }

}
