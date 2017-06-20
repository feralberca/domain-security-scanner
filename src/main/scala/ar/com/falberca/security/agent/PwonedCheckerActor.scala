package ar.com.falberca.security.agent

import com.typesafe.config.ConfigFactory

import java.io.IOException

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpRequest, RequestEntity, Uri}
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.ActorMaterializer

import scala.concurrent.Future
import akka.http.scaladsl.model.Uri.{Path, Query}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.unmarshalling.Unmarshal
import spray.json.DefaultJsonProtocol._
import spray.json._
import akka.http.scaladsl.model.StatusCodes

import scala.util.{Failure, Success}

case class DomainInfo(Name:String, Domain:String)

import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

trait DomainInfoSerialization extends SprayJsonSupport {
  implicit val DomainInfoFormat = jsonFormat2(DomainInfo)
}

import akka.http.scaladsl.unmarshalling.Unmarshal

class PwonedCheckerActor extends Actor with DomainInfoSerialization with ActorLogging {

  private val config = ConfigFactory.load()
  private val pwnedHost = config.getString("pwned.host")
  private val pwnedPort = config.getInt("pwned.port")
  private val pwnedPath = config.getString("pwned.path")

  implicit val system = context.system
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  private val slackNotifierActor = context.actorOf(Props[SlackNotifierActor], "Slack-Notifier-Actor")

  def receive = {
    case domain:String => getDomainInfo(domain).onComplete {
      case Success(domainInfoResult)  => checkDomainStatus(domainInfoResult, domain)
      case Failure(failure) => log.error(s"Error while trying to execute Pwned account. Message:$failure.getMessage")
    }
    case _:Any => sender() !  Future.failed(new UnsupportedOperationException("Invalid Message"))
  }

  private def getDomainInfo(domain:String): Future[List[DomainInfo]] = {
    val httpRequest = HttpRequest(uri = Uri(path = Path(pwnedPath), queryString=Some(s"domain=$domain")))
    log.info(httpRequest.toString())
    val source = Source.single(httpRequest)
    val flow = Http().outgoingConnectionHttps(pwnedHost, pwnedPort)

    source.via(flow).runWith(Sink.head).flatMap { response =>
      response.status match {
        case StatusCodes.OK => {
          if (response.entity.contentType == ContentTypes.`application/json`)
            Unmarshal(response.entity).to[List[DomainInfo]]
          else
            Future.failed(new UnsupportedOperationException(s"Invalid content type in successful response:$response.entity.contentType"))
        }
        case StatusCodes.BadRequest =>
          Future.failed(new UnsupportedOperationException("Incorrent request"))
        case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
          val error = s"Operation Failed with status code ${response.status} and entity $entity"
          Future.failed(new IOException(error))
        }
      }
    }
  }

  private def checkDomainStatus(domainInfoResult:List[DomainInfo], domain:String) = {
    if (!domainInfoResult.isEmpty) {
      val domains = domainInfoResult.map { domainInfo => domainInfo.Domain}.mkString(",")
      val message = s"Security breach detected by site 'Pwned' under domains: $domains"
      slackNotifierActor ! message
      log.info(s"Message delivered to actor: $message")
    }
    else {
      log.info(s"Pwoned site responded with zero breaches for domain: '$domain'")
    }
  }

}
