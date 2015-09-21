import akka.actor.{Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import rest.{SupplierHttpService, RoutesActor}
import spray.can.Http
import utils.{ ActorModuleImpl, ConfigurationModuleImpl}
import scala.concurrent.duration._


object Boot extends App   with ActorModuleImpl {

  // create and start our service actor
  val service = system.actorOf(Props(classOf[RoutesActor]), "routesActor")

  implicit val sys = system
  implicit val timeout = Timeout(5.seconds)

  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ? Http.Bind(service, interface = "localhost", port = 8080)

}
