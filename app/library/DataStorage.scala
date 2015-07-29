package library

import java.io.File
import java.nio.file.{Files, Paths}

import library.Engine._
import models.{Configuration, RawData, Row}
import org.apache.commons.io.FileUtils
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.reflect.io.Path


object DataStorage {
  def configToFilename(caisse: String, config: Configuration, month: String): String = {
    config.metric + "-" + caisse + "-" + config.dimensions.flatten.mkString + "-" + month + ".json"
  }

  def fileExists(filename: String, path: String): Boolean = {
    val listOfFiles: Array[File] = new File(path).listFiles.filter(_.getName.endsWith(".json"))
    listOfFiles.foldLeft(false)((bool, file) => bool | file.getName == filename)
  }

  def storedJsonToRow(json: JsValue, caisse: String, config: Configuration): Future[List[Row]] = {
    val result = json.validate[RawData]
    result.fold(
      errors => {
        println("errors in reading file " + caisse)
        Future.successful(Nil)
      }, rawData =>
        Future.successful(rawData.toRows(caisse)))
  }


  def sendRequestToApiWithStorage(caisse: String, config: Configuration, month: String, numberOfTry: Int): Future[List[Row]] = {
    val path = "jsonDataStorage/"
    if (!Files.exists(Paths.get(path))) {
      Path(path).createDirectory()
    }
    val filename = configToFilename(caisse, config, month)
    if (fileExists(filename, path)) {
      val file: File = new File(path + filename)
      val json = Json.parse(FileUtils.readFileToString(file))
      println("Loading " + filename)
      storedJsonToRow(json, caisse, config)

    }
    else {
      if (numberOfTry < 5) {
        val answer = makeRequest(caisse, config, month)
        answer.flatMap {
          response =>
            response.status match {
              case 204 => Future.successful(List.empty[Row])
              case 200 =>
                val json: JsValue = Json.parse(response.body)
                val result = json.validate[RawData]
                result.fold(
                  errors => {
                    println("errors" + caisse + "on Json parsing")
                    throw new Exception("Failed Json Parsing")
                  }, rawData => {
                    val filename = configToFilename(caisse, config, month)
                    val file = new File(path + filename)
                    FileUtils.write(file, Json.stringify(json))
                    Future.successful(rawData.toRows(caisse))
                  })
              case 400 =>
                if (response.body.toString.contains("exceeded 25.seconds to unspecified while waiting for a response for an individual request, excluding retries")) {
                  sendRequestToApiWithStorage(caisse, config, month, numberOfTry + 1)
                }
                else {
                  println("errors" + caisse + "received a bad request")
                  throw new Exception("Unhandled Bad Request code")
                }
              case _ =>
                println("errors" + caisse + "received an unmanaged code")
                throw new Exception("Unhandled answer code")

            }
        }
      }
      else {
        throw new Exception("Excessive number of try for timeout bad request")
      }
    }

  }
}
