package mesosphere.servicebridge.config

import mesosphere.servicenet.util._

import java.io.File

/**
  * The configuration for a service net instance can be provided with Java
  * properties. For example, when running the project with SBT, you can use
  * `sbt -Dhttp.port=2100 run` to set the web server port.
  *
  * By default, the configuration system will look for properties files at
  * `/usr/local/etc/svcbridge/properties.properties`,
  * `/usr/local/etc/svcbridge.properties`,
  * `/etc/svcbridge/properties.properties`, and `/etc/svcbridge.properties` in
  * that order, and will load the first one found. Properties set in this way
  * will be merged with those set on the command line. To load a different file,
  * pass the property `svcnet.config` or `mesosphere.servicenet.config` on the
  * command line.
  *
  * @param httpPort The port on which to serve web traffic. (Properties:
  *                 `http.port` or `svcbridge.http.port` or
  *                 `mesosphere.servicebridge.http.port`)
  * @param marathon A connection string of the form
  *                 <host>:<port>,<host>:<port>, ...
  */
case class Config(httpPort: Int, marathon: String) extends Logging {

  val propertyLines: Seq[String] = Seq(
    s"svcbridge.marathon=$marathon",
    s"http.port=$httpPort"
  )

  def logSummary() {
    for (line <- propertyLines) log info line
  }
}

object Config extends Logging {
  val fromEnvironment: Map[String, String] = trimmed(Properties.underlying)

  val defaultSearchPath: Seq[String] = Seq(
    "/usr/local/etc/svcbridge/properties.properties",
    "/usr/local/etc/svcbridge.properties",
    "/etc/svcbridge/properties.properties",
    "/etc/svcbridge.properties"
  )

  lazy val fromFiles: Map[String, String] = searchFiles(
    fromEnvironment.get("config").map(Seq(_)).getOrElse(defaultSearchPath)
  )

  def searchFiles(paths: Seq[String] = defaultSearchPath): Map[String, String] = {
    for (f <- paths.map(new File(_)) if f.exists()) {
      log info s"Loading properties from ${f.getAbsolutePath}"
      return trimmed(Properties.load(f))
    }
    Map()
  }

  /**
    * The properties mapping that results from aggregating properties files and
    * command-line properties settings.
    */
  lazy val merged: Map[String, String] = fromFiles ++ fromEnvironment

  def trimmed(properties: Map[String, String]) = Map() ++
    Properties.trim("http", properties, clipPrefix = false) ++
    Properties.trim("mesosphere.servicebridge", properties) ++
    Properties.trim("svcbridge", properties)

  /**
    * Obtain a config, using various defaulting rules to substitute for missing
    * properties. Even a completely disconnected node will be able to get a
    * valid config.
    *
    * @param properties a map of properties (by default, the system properties)
    * @return
    */
  def apply(properties: Map[String, String] = merged): Config =
    Config(
      httpPort = properties.get("http.port").map(_.toInt).getOrElse(9000),
      marathon = properties.get("marathon").getOrElse("localhost:2181")
    )
}
