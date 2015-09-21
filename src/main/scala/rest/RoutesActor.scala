package rest

import akka.actor.{ActorRefFactory, Actor}
import com.romcaste.video.movie.Rating
import com.romcaste.video.movie.impl.MovieImpl
import com.wordnik.swagger.annotations._
import spray.routing.directives.ContentTypeResolver
import scala.concurrent.Future
import com.typesafe.scalalogging.LazyLogging
import spray.httpx.SprayJsonSupport
import spray.routing._
import spray.http._
import MediaTypes._
import utils.{Configuration}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import spray.http.StatusCodes._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import com.gettyimages.spray.swagger._
import com.wordnik.swagger.model.ApiInfo
import scala.reflect.runtime.universe._

class RoutesActor() extends Actor with HttpService with LazyLogging {
  def actorRefFactory = context

  implicit val timeout = Timeout(5.seconds)

  def receive = {
    runRoute(new SupplierHttpService(context).getRoute)
  }
}

@Api(value = "/supplier", description = "Operations about suppliers")
class SupplierHttpService(ctx: ActorRefFactory) extends HttpService {
  import SprayJsonSupport._
  implicit val timeout = Timeout(5.seconds)

  def actorRefFactory = ctx

  @ApiOperation(httpMethod = "GET", response = classOf[MovieImpl], value = "Returns a supplier based on ID")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "supplierId", required = true, dataType = "integer", paramType = "path", value = "The BIG ID of supplier that needs to be fetched")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Ok")))
  def SupplierGetRoute = path("supplier" / IntNumber) { (supId) =>
    get {
      respondWithMediaType(`application/json`) {
        val x = new MovieImpl(
          title = "foo",
          media = com.romcaste.video.movie.MediaType.VHS,
          year = 2008,
          description = "a movie",
          actors = List("joe", "bob"),
          rating = Rating.G)
        complete(x)
      }
    }
  }

  @ApiOperation(value = "Add Supplier", nickname = "addSuplier", httpMethod = "POST", consumes = "application/json", produces = "text/plain; charset=UTF-8")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "body", value = "Supplier Object", dataType = "persistence.entities.SimpleSupplier", required = true, paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Bad Request"),
    new ApiResponse(code = 201, message = "Entity Created")
  ))
  def SupplierPostRoute = path("supplier") {
    post {
      entity(as[MovieImpl]) { (supplierToInsert: MovieImpl) => {
        println("saved to DB" + supplierToInsert)
        complete("ok")
      }
      }
    }
  }

  def getRoute: Route = {
    implicit val context = ctx
    import spray.routing.directives.ContentTypeResolver.Default
    import spray.util.LoggingContext

    val apiDocsHttpSvc = new ApiDocsHttpService(ctx: ActorRefFactory)

    val route: Route = SupplierPostRoute ~ SupplierGetRoute ~ apiDocsHttpSvc.routes ~
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
  override def apiTypes = Seq(typeOf[SupplierHttpService])
  override def apiVersion = "2.0"
  override def baseUrl = "/"
  override def docsPath = "api-docs"
  override def actorRefFactory = ctx
  override def apiInfo =
    Some(
      new ApiInfo(
        "MovieRepositoryManager",
        "An api for managing assets in a movie database",
        "TOC Url",
        "chris@buildlackey.com",
        "Apache V2",
        "http://www.apache.org/licenses/LICENSE-2.0"))
}
