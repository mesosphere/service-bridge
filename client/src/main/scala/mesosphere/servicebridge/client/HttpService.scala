package mesosphere.servicebridge.client

import java.net.URL

trait HttpService {
  def baseUrls: Seq[URL]
}
