import akka.actor.typed.{ActorRef, ActorSystem, Behavior}

import scala.io.StdIn.readLine


object App {

  def readInfo(): (String, Array[String]) = {
    var line: Array[String] = Array()

    while (line.length < 2) {
      println("Enter GoogleAPIKey and tags. Example: GoogleAPIKey tag1 tag2 tag3")
      line = readLine().split("\\s+")
    }

    val key: String = line(0)
    val tags = Array.ofDim[String](line.length - 1)

    for (i <- 1 until line.length) {
      tags(i - 1) = line(i)
    }

    (key, tags)
  }

  def main(args: Array[String]): Unit = {
    val (key, tags) = readInfo()
    ActorSystem[Nothing](Supervisor(key, tags), "iot-system")
  }

}