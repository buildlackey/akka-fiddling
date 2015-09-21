package rest


import com.romcaste.video.movie.{Rating, MediaType}
import com.romcaste.video.movie.impl.MovieImpl
import org.specs2.mutable.Specification
import spray.http.{HttpHeader, StatusCodes, StatusCode}
import spray.routing.HttpService
import spray.testkit.Specs2RouteTest
import scala.concurrent.{Future, future}
import org.mockito.Mockito._
import org.mockito.Matchers._
import org.specs2.specification.BeforeExample
import spray.httpx.unmarshalling._
import spray.httpx.marshalling._
import spray.http.ContentType._
import spray.http._


import spray.http.MediaTypes._

import scalaz.concurrent.Atomic
import akka.actor.{Props, ActorSystem}

class RoutesAndMarshallingTest extends Specification with Specs2RouteTest {
  val svc = new MovieHttpService(system)

  def actorRefFactory = system

  "MovieHttpService" should {
    "return a movie if a match is found in response a search of the form /movies/<title> " in {


      Get("/movies/dogs") ~> svc.getRoute ~> check {
        val movie: String = responseAs[String]
        movie must contain("joe")
      }
    }
    "return 404 (not found) if a /movies/<title> search request is is issued for a non-matching title" in {
      Get("/movies/nonexistent") ~> svc.getRoute ~> check {
        status mustEqual StatusCodes.NotFound
      }
    }
    "return a 201 after successful POST of test fixture that is auto-marshalled to JSON " in {
      Post(
        "/movies",
        new MovieImpl(
          "title1", MediaType.VHS, 2000, "a description", List("mike", "ike"), Rating.PG)) ~>
        svc.getRoute ~>
        check {
          status === StatusCodes.Created
        }
    }
    "return a 201 after successful POST of a hand crafted JSON string test fixture" in {
      val jsonString = getMovieAsJsonString("2000")

      val entity = HttpEntity(`application/json`, jsonString.getBytes)

      Post( "/movies", entity) ~>
        svc.getRoute ~>
        check {
          status === StatusCodes.Created
        }
    }
    "return a 400 (bad request) after failed POST of invalid hand crafted JSON string test fixture" in {
      val jsonString = getMovieAsJsonString("2200")   //  deliberately provide movie with an invalid year
      val entity = HttpEntity(`application/json`, jsonString.getBytes)

      Post( "/movies", entity) ~>
        svc.getRoute ~>
        check {
          System.out.println("status:" + status);
          status === StatusCodes.BadRequest
        }
    }
  }

  def getMovieAsJsonString(year: String): String = {
    s"""{
     "title": "title2",
     "description": "a description",
     "year": $year,
     "rating": "PG",
     "actors": ["mike", "ike"],
     "media": "VHS" }"""
  }
}
