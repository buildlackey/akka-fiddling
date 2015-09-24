package rest

import com.romcaste.video.movie.impl.MovieImpl
import com.romcaste.video.movie.{MediaType, Rating}
import org.specs2.mutable.Specification
import spray.http.ContentType._
import spray.http.MediaTypes._
import spray.http.{StatusCodes, _}
import spray.testkit.Specs2RouteTest

class AnotherRoutesAndMarshallingTest extends Specification with Specs2RouteTest {
  val svc = new MovieHttpService(system)

  sequential

  def actorRefFactory = system

  "MovieHttpService" should {

    "return movie lists correctly sorted in ascending order by title" in {
      Get("/movieSvc/moviesSortedBy?field=TITLE&ascending=true") ~> svc.getRoute ~> check {
        val movies = responseAs[List[MovieImpl]]
        movies.length mustEqual 2
        movies.map {
          _.title
        } mustEqual List("dogs", "pigs")
      }
    }
    "return movie lists correctly sorted in ascending order by year" in {
      Get("/movieSvc/moviesSortedBy?field=YEAR&ascending=true") ~> svc.getRoute ~> check {
        val movies = responseAs[List[MovieImpl]]
        movies.length mustEqual 2
        movies.map {
          _.title
        } mustEqual List("pigs", "dogs")
      }
    }

    "return movie lists correctly sorted by field in descending order by year" in {
      Get("/movieSvc/moviesSortedBy?field=YEAR&ascending=false") ~> svc.getRoute ~> check {
        val movies = responseAs[List[MovieImpl]]
        System.out.println("movies:" + movies);
        movies.length mustEqual 2
        movies.map {
          _.title
        } mustEqual List("dogs", "pigs")
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
