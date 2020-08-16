import akka.actor.typed.Behavior
import akka.actor.typed.PostStop
import akka.actor.typed.Signal
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors


object Supervisor {
  def apply(key: String, tags : Array[String]): Behavior[Nothing] =
    Behaviors.setup[Nothing](context => new Supervisor(context, key, tags))
}

class Supervisor(context: ActorContext[Nothing], key: String, tags : Array[String])
  extends AbstractBehavior[Nothing](context) {

  context.log.info("Application started")
  for (tag <- tags) {
    val actor = context.spawn(NewsActor(key, tag), tag)
    actor ! NewsActor.Tick()
  }

  override def onMessage(msg: Nothing): Behavior[Nothing] = {
    Behaviors.unhandled
  }

  override def onSignal: PartialFunction[Signal, Behavior[Nothing]] = {
    case PostStop =>
      context.log.info("Application stopped")
      this
  }
}