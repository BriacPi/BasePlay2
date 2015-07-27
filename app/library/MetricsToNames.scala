package library

import java.util.concurrent.TimeoutException

import models.Labels
import play.api.Play.current
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WS, WSRequest, WSResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object MetricsToNames {
  def makeRequest(): Future[WSResponse] = {
    val url = "https://vpc-2-bpce-apiu.capback.fr/display/bpce.json"
    val request: WSRequest = WS.url(url)

    val complexRequest: WSRequest =
      request.withHeaders("Accept" -> "application/json")
        .withRequestTimeout(30000)
    complexRequest.get().recoverWith {
      case e: TimeoutException =>
        println("Error in getting Metrics to Names JSON")
        makeRequest()
    }
  }

  def getMapCodesToNames(answer: Future[WSResponse]): Future[Map[String, String]] = {
    answer.flatMap { response =>
      val json: JsValue = Json.parse(response.body)
      val result = json.validate[Labels]
      result.fold(
        errors => {
          println("error in reading CodesToNameJson" +errors)
          getMapCodesToNames(makeRequest())
        }, labels => {
          Future.successful(labels.getCodesToNamesMap)
        })
    }
  }


}
