package ar.com.falberca.security.agent

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}

import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory

import collection.JavaConverters._

/**
  * Main entry point for creating the actor system and loading configuration for actors
  * Created by fernando on 23/05/17.
  */
object MainApp extends App {

  val config = ConfigFactory.load()
  val domains = config.getStringList("security.domains").asScala.toList
  val startup_delay = Duration.apply(config.getDuration("scheduler.startup-delay").toMillis, TimeUnit.MILLISECONDS)
  val repeat_interval = Duration.apply(config.getDuration("scheduler.repeat-interval").toMillis, TimeUnit.MILLISECONDS)

  val system = ActorSystem("SD-Security-App")

  implicit val executor = system.dispatcher

  val pwonedCheckerActor = system.actorOf(Props[PwonedCheckerActor], name = "Pwoned-Check-Actor")

  system.scheduler.schedule(startup_delay, repeat_interval){
    domains.foreach { domain =>
      pwonedCheckerActor ! domain
    }
  }

}
