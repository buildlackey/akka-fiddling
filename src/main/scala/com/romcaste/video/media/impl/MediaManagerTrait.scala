package com.romcaste.video.media.impl

import java.io.{File, InputStream}
import java.util

import com.romcaste.video.media.{impl, Operator}
import com.romcaste.video.movie.impl.MovieImpl
import com.romcaste.video.movie.{Rating, MediaType, Field, Movie}

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

// we separate out implementation of media manager methods into this trait to ease instantiation of objects for testing
//
trait MediaManagerTrait extends com.romcaste.video.media.MediaManager {

  // cannonicalize added movies via a set (mutable for this toy example), and a separate list view of the
  // movie inventory as a slight optimization. (not at all thread-safe or built to scale beyond memory limits)
  //
  private[this] val inventory = scala.collection.mutable.Set[Movie]()
  private[this] var inventoryAsJavaList: util.List[Movie] = new util.ArrayList[Movie]()
  private[this] var inventoryAsList: List[Movie] = Nil

  private[this] def _addMovies(movies: Seq[Movie]): Unit = {
    inventory ++= movies
    inventoryAsList = inventory.toList // representation supporting Scala methods during sort, search, etc
    inventoryAsJavaList = inventory.toList.asJava // for direct returns from getMovies, which want java lists
  }

  // Public methods
  //
  val numFieldsInMovieRecord = 6
  val delimiter = '|'

  override def addMovies(movies: Movie*): Unit = _addMovies(movies)

  override def addMovies(movies: util.List[Movie]): Unit = _addMovies(movies.asScala)

  override def addMovies(file: File): Unit = {
    val movies = InputStreamRecordParser.using(file, numFieldsInMovieRecord, delimiter) {
      (parser: InputStreamRecordParser) => {
        val moviesBuff = new ListBuffer[Movie]
        parser.getRecords foreach
          ((fieldList: Seq[String]) => {
            moviesBuff +=
              new MovieImpl(
                title = fieldList(0).trim,
                description = fieldList(1).trim,
                actors = fieldList(2).trim.split(",").toList.map(_.trim),
                year = java.lang.Short.parseShort(fieldList(3).trim),
                rating = Rating.valueOf(fieldList(4).trim),
                media = MediaType.valueOf(fieldList(5).trim)
              )
          })
        moviesBuff.toList
      }
    }
    _addMovies(movies)
  }

  /**
   * Add movies from input stream. The stream will be closed as a side effect of this method.
   *
   * @param   input  the input stream to read from
   *
   */
  override def addMovies(input: InputStream): Unit = {}

  override def searchMovies(query: String): util.List[Movie] =
    inventoryAsList.filter((m: Movie) => {
      m.asInstanceOf[MovieImpl].containsString(query)
    }).asJava

  type MovieComparator = (MovieImpl, MovieImpl) => Boolean

  private def flipBooleanFunc(func: MovieComparator, doFlip: Boolean): MovieComparator =
    if (doFlip) (m1: MovieImpl, m2: MovieImpl) => !func(m1, m2) else func

  override def sortMovies(field: Field, ascending: Boolean): util.List[Movie] = {
    val sortedList = inventoryAsList.sortWith(
      (m1: Movie, m2: Movie) => {
        val impl1 = m1.asInstanceOf[MovieImpl]
        val impl2 = m2.asInstanceOf[MovieImpl]
        val comparator = flipBooleanFunc(impl1.stateMap(field).comparator, !ascending /* flip if descending*/)
        comparator(impl1, impl2)
      }
    )
    sortedList.asJava
  }


  override def getMovies: util.List[Movie] = inventoryAsJavaList

  override def filterMovies(field: Field, op: Operator, query: String): util.List[Movie] = {
    import com.romcaste.video.movie.impl.MovieImpl

    val filteredList = inventoryAsList.filter(
      (m1: Movie) => {
        val impl1 = m1.asInstanceOf[MovieImpl]
        val filter  = impl1.stateMap(field).filter
        filter(impl1, op, query)
      }
    )
    filteredList.asJava
  }
}
