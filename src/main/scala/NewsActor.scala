import akka.actor.typed.{ActorRef, ActorSystem, Behavior, PostStop, Signal}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors, TimerScheduler}
import akka.http.scaladsl.model.HttpResponse
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

import scala.util.Try

import io.circe.generic.auto._

import scala.concurrent.duration._
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.Http
import akka.http.scaladsl.unmarshalling.Unmarshal
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

import scala.concurrent.Future
import scala.util.Success


final case class Article(publishedAt: String, content: String)
final case class News(articles: List[Article])


object NewsActor {
  def apply(key: String, tag : String): Behavior[Command] =
    Behaviors.setup(context =>
      Behaviors.withTimers(timerScheduler => new NewsActor(timerScheduler, context, key: String, tag : String))
    )

  sealed trait Command

  final case class Tick() extends Command

  final case class HasNews(news: Try[News]) extends Command

}

class NewsActor(timers: TimerScheduler[NewsActor.Command],
                context: ActorContext[NewsActor.Command],
                key: String,
                tag : String)
  extends AbstractBehavior[NewsActor.Command](context) {

  import NewsActor._

  context.log.info("NewsActor for tag {} started", tag)

  timers.startTimerWithFixedDelay("test", Tick(), 1.minute)

  def read(key: String, tag: String): Unit = {
    implicit val system: ActorSystem[Nothing] = context.system
    implicit val postCodec: Codec[News] = deriveCodec

    val uri: String = "https://newsapi.org/v2/everything?q=" + tag + "&apiKey=" + key

    val response: Future[HttpResponse] = Http().singleRequest(Get(uri))

    val post: Future[News] = response.flatMap(Unmarshal(_).to[News])(context.executionContext)

    context.pipeToSelf(post)(result => HasNews(result))
  }

  def hasNews(news: Try[News], tag: String): Unit = {
    news match {
      case Success(post) => {
        var timestamp = "0"
        var content = "No results"
        var news = post.articles
        timestamp = news(0).publishedAt
        content = news(0).content
        println(timestamp + " " + tag + " " + content + "\n")
      }
      case _ => {
        context.log.info("Not success", tag)
      }
    }
  }

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case Tick() =>
        read(key, tag)
        this
      case HasNews(news) =>
        hasNews(news, tag)
        this


    }
  }

  override def onSignal: PartialFunction[Signal, Behavior[Command]] = {
    case PostStop =>
      context.log.info("News actor {} stopped", tag)
      this
  }

}