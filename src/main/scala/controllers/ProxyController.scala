package controllers

import javax.inject._
import net.wiringbits.proxy.services.ProxyService
import org.slf4j.LoggerFactory
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@Singleton
class ProxyController @Inject()(cc: ControllerComponents, proxyService: ProxyService)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def health() = Action {
    Ok("")
  }

  def resolve() = Action.async { request =>
    val jsonInput = request.body.asJson.flatMap { json =>
      for {
        url <- (json \ "url").asOpt[String]
        headers = (json \ "headers").asOpt[Map[String, String]].getOrElse(Map.empty)
      } yield url -> headers
    }

    jsonInput match {
      case Some((url, headers)) =>
        proxyService
          .call(url, headers)
          .map { response =>
            Status(response.status)(response.body)
          }
          .recover {
            case NonFatal(ex) =>
              logger.info(s"Failed calling: $url")
              InternalServerError(ex.getMessage)
          }

      case None =>
        val msg =
          """
            |The expected format is:
            |{
            |  "url": "https://wiringbits.net",
            |  "headers": {
            |    "Content-type": "application/json"
            |  }
            |}
            |""".stripMargin.trim
        Future.successful(BadRequest(msg))
    }
  }
}
