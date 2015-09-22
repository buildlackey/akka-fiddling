package com.romcaste.video.movie.impl

import akka.actor.FSM.->
import com.romcaste.video.media.Operator
import com.romcaste.video.movie.Field._
import com.romcaste.video.movie.{Field, MediaType, Movie, Rating}
import spray.httpx.SprayJsonSupport
import spray.json._

final case class MovieImpl(title: String,
                      media: MediaType,
                      year: Short,
                      description: String,
                      actors: List[String],
                      rating: Rating) extends Movie {
  require(title != null && !title.isEmpty, "title can not be empty")
  require(media != null)
  require(description != null) // empty description could make sense, so allow it
  require(actors != null)
  require(rating != null)

  // movies will likely be replaced by something at this point in future...
  require(year < 2100, "specified year is to far in the future")

  // flatten out possible dups in list into one cannonical name, and then sort list so equals works sensibly
  private[this] val actorList: Array[String] = (actors.toSet.toArray: Array[String]).sorted


  // Created the class below in order to hold a representation of a Movie whose fields can be easily compared and
  // reliably folded into a hashcode computation.  The interface requirement that actor list is returned as a Java array
  // complicates the Scala implementation because Java Array's hashcode implementation is based on reference, not array
  // contents, and also because arrays are not comparable in the context of  Scala collections sort method, not to
  // mention that the toString representation of arrays gives us a jumble of characters.
  //
  // So, this internal class helps us deal with all fields in a more maintainable, performant way, and keeps the actor
  // list in its original form (for when we give it back via getActors), and as well as  in a form that is more
  // conducive  to being sorted, printed out, searched, etc. Key Point: The primary reason for having
  // 'fieldValAsScala' is to store the two necessary representations of the actor list.
  //
  // We are trading a little internal complexity for maintainability and performance. If we didn't do this, we'd have
  // have swtich statements proliferating throughout the code -- which we would need to change if we were to
  // add/change the fields of this class in the future.
  //

  type MovieComparator = (MovieImpl, MovieImpl) => Boolean
  type MovieFilter = (MovieImpl, Operator, String) => Boolean

  case class FieldRepresentation(field: Field,
                                 comparator: MovieComparator,
                                 filter: MovieFilter,
                                 fieldVal: Object,
                                 fieldValAsScala: Object) {}


  // comparator for fields that lack a nice natual ordering  (especially, Java types like enums, array lists, etc.)
  //
  private def compareViaField(field: Field)(m1: MovieImpl, m2: MovieImpl): Boolean = {
    if (field == ACTORS) {
      MovieImpl.compareLists(
        m1.stateMap(ACTORS).fieldValAsScala.asInstanceOf[List[String]],
        m2.stateMap(ACTORS).fieldValAsScala.asInstanceOf[List[String]])
    } else m1.getField(field).toString < m2.getField(field).toString
  }

  private def filterByField(field: Field)(movie: MovieImpl, op: Operator, criteria: String) = {
    if (op == Operator.CONTAINS) {
      movie.getField(field).toString.contains(criteria)
    } else if (op == Operator.EQUALS) {
      movie.getField(field).toString.equals(criteria)
    } else if (op == Operator.LESS_THAN) {
      movie.getField(field).toString < criteria
    } else if (op == Operator.GREATER_THAN) {
      movie.getField(field).toString > criteria
    } else {
      throw new IllegalArgumentException("unrecognized operator: " + op)
    }
  }

  private def filterByActorsField(movie: MovieImpl, op: Operator, criteria: String) = {
    val actors = movie.stateMap(ACTORS).fieldValAsScala.asInstanceOf[List[String]]
    if (op == Operator.CONTAINS) {
      actors.exists( (a:String) =>  { a .contains(criteria) })
      actors.exists(_ .contains(criteria))
    } else if (op == Operator.EQUALS) {
      actors.exists(_ .equals(criteria))
    } else if (op == Operator.LESS_THAN) {
      ! actors.exists(_ >= criteria)
    } else if (op == Operator.GREATER_THAN) {
      ! actors.exists(_ <= criteria)
    } else {
      throw new IllegalArgumentException("unrecognized operator: " + op)
    }
  }

  val stateMap: Map[Field, FieldRepresentation] = Map(
    TITLE ->
      FieldRepresentation(
        TITLE, _.getTitle < _.getTitle, filterByField(TITLE), title, title),
    YEAR ->
      FieldRepresentation(
        YEAR, _.getYear < _.getYear, filterByField(YEAR), new java.lang.Short(year), year.toString),
    MEDIA ->
      FieldRepresentation(
        MEDIA, compareViaField(MEDIA), filterByField(MEDIA), media, media.toString),
    DESCRIPTION ->
      FieldRepresentation(
        DESCRIPTION, compareViaField(DESCRIPTION), filterByField(DESCRIPTION), description, description),
    ACTORS ->
      FieldRepresentation(
        ACTORS, compareViaField(ACTORS), filterByActorsField, actorList, actorList.toList),
    RATING ->
      FieldRepresentation(
        RATING, compareViaField(RATING), filterByField(RATING), rating, rating.toString)
  )

  private[this] val stateValues = stateMap.values.toList.map(_.fieldValAsScala)

  def containsString(str: String) = stateValues exists (_.toString.toLowerCase.contains(str.toLowerCase))

  override def getField(field: Field): Object = stateMap(field).fieldVal

  override def getTitle: String = title

  override def getMedia: MediaType = media

  override def getYear: Short = year

  override def getDescription: String = description

  override def getRating: Rating = rating

  override def getActors: Array[String] = actorList

  private def compare(v1: Any, v2: Any): Boolean = {
    v1 == v2
  }

  override def equals(other: Any): Boolean = other match {
    case that: MovieImpl =>
      (this.stateMap.values zip that.stateMap.values).
        forall {
        case (thisProp: this.FieldRepresentation, thatProp: that.FieldRepresentation) =>
          compare(thisProp.fieldValAsScala, thatProp.fieldValAsScala)
      }
    case _ =>
      false
  }

  override def hashCode(): Int = {
    stateValues.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString = {
    val fields = stateMap.toList map {
      case (x, y) => s"$x=${
        y.fieldValAsScala
      }"
    } mkString ", "
    s"MovieImpl($fields)"
  }
}


object MovieImpl extends DefaultJsonProtocol with SprayJsonSupport {

  // compareLists: comparator for sorting by actor list - kept in companion object so we can more easily test it.
  //
  // return true if list1 is less than list2, using the natural ordering of the contained elements.
  //
  // we recursively examine the head of list1 and list2, and continue to work through the lists as long
  // as the head elements are equal. As soon as we find  case where the heads are different, the result of
  // the overall comparison is the "isLessThan" result of comparing the  two head elements, where the absence of
  // a head element in one of the lists (and presence of same in the other) means the list without the
  // head element 'is less than' the other list. If we work through both lists and they are the same length and
  // all elements are equal the function will return false.
  //
  private[impl] def compareLists[A](list1: List[A], list2: List[A])
                                   (implicit ev1: A => Ordered[A]): Boolean = {
    list1 match {
      case head1 :: tail1 => {
        list2 match {
          case head2 :: tail2 => if (head1 == head2) compareLists(tail1, tail2) else head1 < head2
          case Nil => false
        }
      }
      case Nil => list2.nonEmpty
    }
  }

  // implicit JSON marshaller for MediaType
  implicit object mediaTypeJsonFormat extends RootJsonFormat[MediaType] {
    def write(mtype: MediaType) = JsString(mtype.toString)

    def read(value: JsValue) = value match {
      case JsString(mtype) => MediaType.valueOf(mtype)
      case _ => deserializationError("MediaType string expected")
    }
  }

  // implicit JSON marshaller for Rating
  implicit object ratingJsonFormat extends RootJsonFormat[Rating] {
    def write(rating: Rating) = JsString(rating.toString)

    def read(value: JsValue) = value match {
      case JsString(rating) => Rating.valueOf(rating)
      case _ => deserializationError("Rating string expected")
    }
  }


  implicit def userJsonFormat: RootJsonFormat[MovieImpl] =
    jsonFormat(MovieImpl.apply, "title", "media", "year", "description", "actors", "rating")
}


