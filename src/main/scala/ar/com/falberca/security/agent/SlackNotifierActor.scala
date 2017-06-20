package ar.com.falberca.security.agent

import java.io.IOException

import akka.actor.{Actor, ActorLogging}
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future
import scala.util.{Failure, Success}

class SlackNotifierActor  extends Actor with ActorLogging {

  private val config = ConfigFactory.load()
  private val slackHost = config.getString("slack.host")
  private val slackPort = config.getInt("slack.port")
  private val slackPath = config.getString("slack.path")

  implicit val system = context.system
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  def receive = {
    case message:String => postMessageInChannel(message).onComplete {
      case Success(status)  => log.info(s"Notification process completed! with status: $status")
      case Failure(failure) => log.error(s"Notification process failure. Message:$failure.getMessage")
    }
    case _:Any => sender() !  Future.failed(new UnsupportedOperationException("Invalid Message"))
  }

  private def postMessageInChannel(message:String): Future[String] = {

    val httpPostRequest = RequestBuilding.Post(Uri(path = Path(slackPath)),
                                    HttpEntity(s"""{"text":"$message"}"""))
    log.info(httpPostRequest.toString())
    val source = Source.single(httpPostRequest)
    val flow = Http().outgoingConnectionHttps(slackHost, slackPort)

    source.via(flow).runWith(Sink.head).flatMap { response =>
      response.status match {
        case StatusCodes.OK => Future.successful("Message successfully delivered to Slack API")
        case _ => Unmarshal(response.entity).to[String].flatMap { entity =>
          val error = s"Operation Failed with status code ${response.status} and entity $entity"
          Future.failed(new IOException(error))
        }
      }
    }
  }

}
