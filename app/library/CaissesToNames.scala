package library

import java.util.concurrent.TimeoutException

import models.Labels
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WS, WSRequest, WSResponse}
import play.api.Play.current
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object CaissesToNames {
  def makeRequest(): Future[WSResponse] = {
    val url = "https://s3-eu-west-1.amazonaws.com/production-visualizations/bpce-labels.js"
    val request: WSRequest = WS.url(url)

    val complexRequest: WSRequest =
      request.withHeaders("Accept" -> "application/json")
        .withRequestTimeout(30000)
    complexRequest.get()
  }

  def getMapCaissesToNames(answer: Future[WSResponse]): Future[Map[String, String]] = {
    answer.flatMap { response =>
      val text = response.body.replaceAll("cv.labels =","").dropRight(1)
      val json: JsValue = Json.parse(text)
      val result = json.validate[Labels]
      result.fold(
        errors => {
          println("error in reading CodesToNameJson" +errors)
          getMapCaissesToNames(makeRequest())
        }, labels => {
          Future.successful(labels.getCaissesToNamesMap)
        })
    }
  }


}
