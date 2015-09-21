package rest


import org.specs2.mutable.Specification
import spray.routing.HttpService
import spray.testkit.Specs2RouteTest
import utils.ActorModuleImpl
import scala.concurrent.{Future, future}
import org.mockito.Mockito._
import org.mockito.Matchers._
import org.specs2.specification.BeforeExample

import scalaz.concurrent.Atomic
import akka.actor.{Props, ActorSystem}

class RoutesAndMarshallingTest extends Specification with Specs2RouteTest {

  val svc = new SupplierHttpService(system)


  def actorRefFactory = system

  //override val userRepository: UserRepository = mock(classOf[UserRepository])

  "SupplierHttpService" should {
    "return a greeting for GET requests to the root path" in {

      //when(userRepository.fetch(anyString())).thenReturn( future{new MovieImpl("greeting", 1, MediaType.VHS, List())} )


      Get("/supplier/1") ~> svc.getRoute ~> check {
        val movie: String = responseAs[String]
        System.out.println("as string movie:" + movie);

        movie must contain("joe")
      }
    }
  }
}
