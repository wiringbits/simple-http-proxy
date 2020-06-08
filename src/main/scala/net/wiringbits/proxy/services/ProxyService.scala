package net.wiringbits.proxy.services

import javax.inject.Inject
import org.slf4j.LoggerFactory
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext

class ProxyService @Inject()(ws: WSClient)(implicit ec: ExecutionContext) {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def call(url: String, headers: Map[String, String]) = {
    val stringHeaders = headers.map { case (key, value) => s"$key -> $value" }.mkString("\n")
    logger.info(s"Calling: $url, headers = $stringHeaders")

    ws.url(url)
      .withHttpHeaders(headers.toList: _*)
      .execute()
  }
}
