package library

import java.util.concurrent.TimeoutException

import models.{Metric, Metrics}
import play.api.Play.current
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WS, WSRequest, WSResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object MetricsToNames {
  def makeMetricRequest(): Future[WSResponse] = {
    val url = "https://vpc-2-bpce-apiu.capback.fr/display/bpce.json"
    val request: WSRequest = WS.url(url)

    val complexRequest: WSRequest =
      request.withHeaders("Accept" -> "application/json")
        .withRequestTimeout(30000)
    complexRequest.get()
  }

  def getMapMetricsToNames(answer: Future[WSResponse]): Future[Metrics] = {
    answer.flatMap { response =>
      val json: JsValue = Json.parse(response.body)
      val result = json.validate[Metrics]
      result.fold(
        errors => {
          println("error in reading CodesToNameJson" + errors)
          getMapMetricsToNames(makeMetricRequest())
        }, metrics => {
          Future.successful(metrics)
        })
    }
  }


}
