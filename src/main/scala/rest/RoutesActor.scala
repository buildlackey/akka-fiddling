package rest

import java.util
import javax.ws.rs.Path

import akka.actor.{ActorRefFactory, Actor}
import com.romcaste.video.media.Operator
import com.romcaste.video.media.impl.MediaManagerTrait
import com.romcaste.video.movie.{Field, Movie, Rating}
import com.romcaste.video.movie.impl.MovieImpl
import com.wordnik.swagger.annotations._
import spray.http.HttpHeaders.{Location, RawHeader}
import spray.httpx.marshalling.Marshaller
import spray.json.{JsValue, JsString, RootJsonFormat}
import spray.routing.directives.ContentTypeResolver
import spray.util.LoggingContext
import scala.concurrent.Future
import com.typesafe.scalalogging.LazyLogging
import spray.httpx.SprayJsonSupport
import spray.routing._
import spray.http._
import MediaTypes._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import spray.http.StatusCodes._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import com.gettyimages.spray.swagger._
import com.wordnik.swagger.model.ApiInfo
import scala.reflect.runtime.universe._
import scala.collection.JavaConverters._
import language.postfixOps

class RoutesActor() extends Actor with HttpService with LazyLogging {
  def actorRefFactory = context

  implicit val timeout = Timeout(5.seconds)

  def receive = {
    runRoute(new MovieHttpService(context).getRoute)
  }

}


@Api(value = "/movieSvc", description = "Movie inventory management service")
class MovieHttpService(ctx: ActorRefFactory) extends HttpService with MediaManagerTrait {

  import SprayJsonSupport._
  import com.romcaste.video.movie.Field._
  import com.romcaste.video.movie.MediaType._
  import com.romcaste.video.media.Operator._
  import com.romcaste.video.movie.impl.MovieImpl

  implicit val timeout = Timeout(5.seconds)

  // Initialize with some dummy movies to exercise REST API
  addMovies(
    new MovieImpl(
      title = "dogs",
      media = com.romcaste.video.movie.MediaType.VHS,
      year = 2008,
      description = "a movie about dogs",
      actors = List("joe", "bob"),
      rating = Rating.G),
    new MovieImpl(
      title = "pigs",
      media = com.romcaste.video.movie.MediaType.VHS,
      year = 2000,
      description = "a movie about pigs",
      actors = List("rover", "porky"),
      rating = Rating.G))


  def actorRefFactory = ctx


  @Path("/movies/{title}")
  @ApiOperation(
    httpMethod = "GET",
    response = classOf[MovieImpl],
    value = "Returns a movie based on title passed as the final path parameter, or all movies if no param is provided")
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        name = "title",
        required = false,
        dataType = "string",
        paramType = "path",
        value = "Title of movie to be returned")
    ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Ok")))
  def MovieGetRoute = get {
    path("movieSvc" / "movies" / Segment) { (title: String) =>
      respondWithMediaType(`application/json`) {
        val movieList = filterMovies(TITLE, EQUALS, title)
        if (movieList.isEmpty) complete(NotFound) else complete(movieList.get(0).asInstanceOf[MovieImpl])
      }
    }
  } ~ MovieListGetRoute ~ MovieSortedByGetRoute ~ MovieFilteredByGetRoute

  @Path("/movies/")
  @ApiOperation(
    httpMethod = "GET",
    response = classOf[List[MovieImpl]],
    value = "Returns a list of all movies in the current inventory - don't forget trailing slash !")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Ok")))
  def MovieListGetRoute = get {
    path("movieSvc" / "movies" /) {
      respondWithMediaType(`application/json`) {
        complete {
          val movieList: util.List[Movie] = sortMovies(TITLE, true)
          val movieImplList = movieList.asScala.map {
            _.asInstanceOf[MovieImpl]
          }.toList
          System.out.println(">>>>Here is the movieImplList:" + movieImplList);
          movieImplList.toList
        }
      }
    }
  }


  @Path("/moviesSortedBy")
  @ApiOperation(
    httpMethod = "GET",
    response = classOf[List[MovieImpl]],
    value = "Returns a list of movies sorted by the specified field in either ascending or descending order")
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        name = "field",
        required = false,
        dataType = "string",
        paramType = "query",
        value = "field by which list of movies will be sorted"),
      new ApiImplicitParam(
        name = "ascending",
        required = true,
        dataType = "boolean",
        paramType = "query",
        value = "true if movies are to be sorted ascending order, else false")
    )
  )
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Ok")))
  def MovieSortedByGetRoute = get {
    path("movieSvc" / "moviesSortedBy") {
      parameter("field") { (field: String) =>
        parameter('ascending.as[Boolean]) { (ascending: Boolean) =>
          respondWithMediaType(`application/json`) {
            val movies: List[Movie] = sortMovies(Field.valueOf(field), ascending).asScala.toList
            val movieImplList = movies.map {
              _.asInstanceOf[MovieImpl]
            }.toList
            complete(movieImplList)
          }
        }
      }
    }
  }


  @Path("/moviesFilteredBy")
  @ApiOperation(
    httpMethod = "GET",
    response = classOf[List[MovieImpl]],
    value = "Returns movies matching constraint that <specified-field> <specified-operator> <constant> is true")
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        name = "field",
        required = false,
        dataType = "string",
        paramType = "query",
        value = "field selected to be compared against constant"),
      new ApiImplicitParam(
        name = "operator",
        required = true,
        dataType = "string",
        paramType = "query",
        value = "one of the following: CONTAINS, EQUALS, LESS_THAN, GREATER_THAN"),
      new ApiImplicitParam(
        name = "constant",
        required = true,
        dataType = "string",
        paramType = "query",
        value = "a URL encoded string value")
    )
  )
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Ok")))
  def MovieFilteredByGetRoute = get {
    path("movieSvc" / "moviesFilteredBy") {
      parameter("field") { (field: String) =>
        parameter("operator") { (operator: String) =>
          parameter("constant") { (constant: String) =>
            respondWithMediaType(`application/json`) {
              val movies: List[Movie] =
                filterMovies(
                  Field.valueOf(field), Operator.valueOf(operator), constant).asScala.toList
              val movieImplList = movies.map {
                _.asInstanceOf[MovieImpl]
              }.toList
              complete(movieImplList)
            }
          }
        }
      }
    }
  }

  @Path("/movies")
  @ApiOperation(
    value = "Adds a Movie - note that there is a bug with the Swagger documentation - you have to enter 'year' by hand",
    nickname = "addMovie",
    httpMethod = "PUT",
    consumes = "application/json",
    produces = "application/json;charset=UTF-8")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "body",
      value = "Movie Object",
      dataType = "MovieImpl",
      required = true,
      paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Bad Request"),
    new ApiResponse(code = 201, message = "Entity Created")
  ))
  def MoviePostRoute = path("movieSvc" / "movies") {
    put {
      requestInstance {
        request =>
          entity(as[MovieImpl]) {
            (movieToInsert: MovieImpl) => {
              addMovies(movieToInsert)
              val pathToNewResource = request.uri.withPath(request.uri.path / movieToInsert.getTitle)
              respondWithHeaders(Location(pathToNewResource)) {
                println("movie list is now:" + getMovies)
                complete(StatusCodes.Created)
              }
            }
          }
      }
    }
  }


  def getRoute: Route = {
    implicit val context = ctx

    import spray.routing.directives.ContentTypeResolver.Default
    import spray.util.LoggingContext

    val exceptionHandler = ExceptionHandler {
      case e: IllegalArgumentException =>
        requestUri {
          uri =>
            complete(BadRequest, "Invalid request" + e.getMessage)
        }
    }

    val rejectionHandler = RejectionHandler {
      case MalformedRequestContentRejection(message, cause) :: _ =>
        complete(BadRequest, "Invalid Request: " + cause.getOrElse(""))
    }

    val apiDocsHttpSvc = new ApiDocsHttpService(ctx: ActorRefFactory)

    val route: Route = handleExceptions(exceptionHandler) {
      handleRejections(rejectionHandler) {
        MoviePostRoute ~ MovieGetRoute
      }
    } ~
      apiDocsHttpSvc.routes ~
      get {
        pathPrefix("") {
          pathEndOrSingleSlash {
            getFromResource("swagger-ui/index.html")
          }
        } ~
          getFromResourceDirectory("swagger-ui")
      }
    route
  }
}


class ApiDocsHttpService(ctx: ActorRefFactory) extends SwaggerHttpService {
  override def apiTypes = Seq(typeOf[MovieHttpService])

  override def apiVersion = "2.0"

  override def baseUrl = "/"

  override def docsPath = "api-docs"

  override def actorRefFactory = ctx

  override def apiInfo =
    Some(
      new ApiInfo(
        "Movie Repository Management API",
        "An api for managing assets in a movie database",
        "TOC Url",
        "chris@buildlackey.com",
        "Apache V2",
        "http://www.apache.org/licenses/LICENSE-2.0"))
}
