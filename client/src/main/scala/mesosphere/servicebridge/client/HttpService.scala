package mesosphere.servicebridge.client

trait HttpService {
  val host: String
  val port: Int
  def baseUrl: String = s"http://${host}:${port}"
}
