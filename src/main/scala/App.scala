import akka.actor.{Actor, Props}

import scala.language.postfixOps
import scala.io.StdIn.readLine
import scala.concurrent.duration._
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import io.circe._
import io.circe.generic.auto._
import io.circe.generic.semiauto._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.parser._
import io.circe.syntax._

import scala.concurrent.Future
import scala.util.Success


object App {

  final case class Article(publishedAt: String, content: String)
  final case class Post(articles: List[Article])

  implicit val postCodec: Codec[Post] = deriveCodec

  implicit val system = ActorSystem(Behaviors.empty, "GetMeNews")
  implicit val context = system.executionContext

  trait NewsService {
    def getNews(): Unit
  }

  final case class NewsServiceImpl(key: String, tags: Array[String]) extends NewsService {
    override def getNews(): Unit = {
      println("Test")
      for (tag <- tags) {
        val uri: String = "https://newsapi.org/v2/everything?q=" + tag + "&apiKey=" + key
        val response: Future[HttpResponse] = Http().singleRequest(Get(uri))
        val post: Future[Post] = response.flatMap(Unmarshal(_).to[Post])

        post.onComplete {
          case Success(post) => {
            var timestamp = "0"
            var content = "no results"
            var news = post.articles
            timestamp = news(0).publishedAt
            content = news(0).content
            println(timestamp + " " + tag + " " + content + "\n")
          }
          case _ => {
            println("post was not success")
          }
        }
      }
    }
  }

  final class NewsActor(newsService: NewsService) extends Actor {
     def receive = {
       case "do" => newsService.getNews()
     }
  }

  final class timerActor(duration: Duration, newsService: NewsService) {
    def runActor : Unit = {
     // val newsActor = system.actorOf(Props(classOf[NewsActor], newsService))
      //system.scheduler.scheduleWithFixedDelay(Duration.Zero, duration, newsActor, "do")
    }
  }


  def main(args: Array[String]) = {

    var line : Array[String] = Array()
    while (line.length < 2) {
      println("Enter GoogleAPIKey and tags. Example: GoogleAPIKey tag1 tag2 tag3")
      line = readLine().split("\\s+")
    }
    val key: String = line(0)
    var tags = Array.ofDim[String](line.length - 1)

    for (i <- 1 until line.length) {
      tags(i - 1) = line(i)
    }

    val newsService = new NewsServiceImpl(key, tags)
    val timerActor = new timerActor(1 seconds, newsService)
    timerActor.runActor
  }
}