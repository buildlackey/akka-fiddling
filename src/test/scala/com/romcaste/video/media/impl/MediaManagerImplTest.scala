package com.romcaste.video.media.impl

import java.io.{File, PrintWriter}
import java.util

import com.romcaste.video.media.{Operator, MediaManager}
import com.romcaste.video.movie.Field._
import com.romcaste.video.media.Operator._
import com.romcaste.video.movie.impl.MovieImpl
import com.romcaste.video.movie.{Field, MediaType, Movie, Rating}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.{FlatSpec, ShouldMatchers}

import java.io.File
import com.romcaste.video.media.impl.RichFile._

import scala.collection.JavaConverters._
import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class MediaManagerImplTest extends FlatSpec with ShouldMatchers with TempFolderTestingFixture {


  // Fixtures used in various tests in suite
  //
  val m1: Movie =
    new MovieImpl(
      title = "a",
      media = MediaType.VHS,
      year = 2001,
      description = "aMovie ",
      actors = List("a-actor", "b-actor", "c-actor"),
      rating = Rating.G)

  val m2: Movie =
    new MovieImpl(
      title = "b",
      media = MediaType.LAZERDISK,
      year = 2002,
      description = "bMovie ",
      actors = List("d-actor"),
      rating = Rating.PG)

  val m3: Movie =
    new MovieImpl(
      title = "c",
      media = MediaType.DVD,
      year = 2013,
      description = "cMovie ",
      actors = List("g-actor", "h-actor", "i-actor"),
      rating = Rating.PG_13)

  val m4: Movie =
    new MovieImpl(
      title = "d",
      media = MediaType.VCD,
      year = 2014,
      description = "dMovie ",
      actors = List("j-actor", "k-actor", "l-actor"),
      rating = Rating.R)

  "A MediaManagerImpl's 'add to inventory' capabilities" should
    "add in varargs lists of movies identically to adding a List<Movie>" in {
    val mgr1: MediaManager = new Object with MediaManagerTrait
    val mgr2: MediaManager = new Object with MediaManagerTrait

    mgr1.addMovies(m3, m2, m4, m1)
    mgr2.addMovies(List(m3, m2, m4, m1).asJava)

    mgr1.getMovies.size shouldEqual 4
    mgr2.getMovies shouldEqual mgr1.getMovies
  }
  it should "treat additions of the same movie idempotently (i.e., as a no-op when added the second time)" in {

    val mgr1: MediaManager = new Object with MediaManagerTrait
    mgr1.addMovies(m1)
    mgr1.addMovies(m1)
    mgr1.getMovies.size() shouldEqual 1
    mgr1.getMovies.get(0) shouldEqual m1
  }
  it should "reflect 'no inventory' if addMovies with an empty list of movies is invoked on a new instance" in {
    val mgr1: MediaManager = new Object with MediaManagerTrait
    mgr1.addMovies(new util.ArrayList[Movie]())
    mgr1.getMovies.size() shouldEqual 0
  }
  it should "cause repository to increase in size as new movies are added" in {
    val mgr1: MediaManager = new Object with MediaManagerTrait

    mgr1.addMovies(m1)
    mgr1.addMovies(m2)
    mgr1.getMovies.size() shouldEqual 2
  }


  // Fixtures used in tests of addition to inventory by file, and filtering, and search
  //
  val mediaMgr: MediaManager = new Object with MediaManagerTrait
  val inputFile: File = new File(testFolder, "input1")
  inputFile.text =
    """|a| classic | Larry, Moe, Curly  | 1939 | G | VHS
      |b| funny   | larry, moe, curly   | 2001 | PG_13 | DVD""".stripMargin

  mediaMgr.addMovies(inputFile)

  def doSearch(sought: String, mgr: MediaManager): List[String] =
    mgr.searchMovies(sought).asScala.toList.map(_.getTitle).sorted

  it should "correctly read and parse entries from a file" in {
    val moviesSortedByTitle = mediaMgr.sortMovies(TITLE, true).asScala
    moviesSortedByTitle.map(_.getTitle) shouldEqual List("a", "b")
    moviesSortedByTitle(1).getActors.sorted shouldEqual List("curly", "larry", "moe")
  }

  "A MediaManagerImpl's sorting, filtering and searching features " should
    "sort correctly by Field in either ascending or descending order" in {
    type Field = com.romcaste.video.movie.Field

    val expectedSortResults =
      Table(
        ("field", "sortAscending?", "expected results: as list of movie names"),
        (TITLE, true, List("a", "b", "c", "d")),
        (TITLE, false, List("a", "b", "c", "d").reverse),
        (MEDIA, true, List("c", "b", "d", "a")),
        (MEDIA, false, List("c", "b", "d", "a").reverse),
        (YEAR, true, List("a", "b", "c", "d")),
        (YEAR, false, List("a", "b", "c", "d").reverse),
        (DESCRIPTION, true, List("a", "b", "c", "d")),
        (DESCRIPTION, false, List("a", "b", "c", "d").reverse),
        (ACTORS, true, List("a", "b", "c", "d")),
        (ACTORS, false, List("a", "b", "c", "d").reverse),
        (RATING, true, List("a", "b", "c", "d")),
        (RATING, false, List("a", "b", "c", "d").reverse)
      )
    val mgr1: MediaManager = new Object with MediaManagerTrait

    mgr1.addMovies(m1, m2, m3, m4)

    forAll(expectedSortResults) {
      (field: Field, ascending: Boolean, expectedResult: List[String]) => {
        val actualResult = mgr1.sortMovies(field, ascending).asScala.toList map {
          _.getTitle
        }
        actualResult shouldEqual expectedResult
      }
    }
  }

  it should "be able to filter by comparing movie field's using a constant value and a constraint (== != < >) " in {

    val expectedFilterResults =
      Table(
        ("field", "operator", "constant", "expected results: as list of movie names"),
        (TITLE, EQUALS, "a", List("a")),
        (TITLE, GREATER_THAN, "a", List("b")),
        (DESCRIPTION, CONTAINS, "lassic", List("a")),
        (DESCRIPTION, CONTAINS, "LASSIC", List()),
        (DESCRIPTION, LESS_THAN, "funny", List("a")),
        (DESCRIPTION, LESS_THAN, "zunny", List("a", "b")),
        (ACTORS, CONTAINS, "arry", List("a", "b")),
        (ACTORS, CONTAINS, "none", List()),
        (ACTORS, LESS_THAN, "Burly", List()),
        (ACTORS, LESS_THAN, "curly", List("a")),
        (ACTORS, LESS_THAN, "zoe", List("a", "b")),
        (YEAR, CONTAINS, "39", List("a")),
        (YEAR, GREATER_THAN, "1940", List("b")),
        (RATING, CONTAINS, "G", List("a", "b")),
        (RATING, EQUALS, "PG_13", List("b")),
        (MEDIA, LESS_THAN, "A", List()),
        (MEDIA, LESS_THAN, "Z", List("a", "b")),
        (MEDIA, EQUALS, "DVD", List("b"))
      )

    forAll(expectedFilterResults) {
      (field: Field,  op: Operator, filterCriteria: String, expectedResult: List[String]) => {
        val actualResult = mediaMgr.filterMovies(field, op, filterCriteria).asScala.toList.map { _.getTitle }.sorted
        actualResult shouldEqual expectedResult
      }
    }
  }

  it should "not blow up if there are no movies in inventory" in {
    val mgr1: MediaManager = new Object with MediaManagerTrait
    doSearch("b", mgr1) shouldEqual List()
  }
  it should "enable case insensitive search such that a match is triggered if the string occurs in any field" in {
    val mgr1: MediaManager = new Object with MediaManagerTrait
    mgr1.addMovies(m1, m2, m3, m4)

    doSearch("b", mgr1) shouldEqual List("a", "b")
    doSearch("h", mgr1) shouldEqual List("a", "c")
    doSearch("2014", mgr1) shouldEqual List("d")
    doSearch("aMovie", mgr1) shouldEqual List("a")
    doSearch("j-act", mgr1) shouldEqual List("d")
    doSearch("pg", mgr1) shouldEqual List("b", "c")
  }
  it should "return empty list if no movies match given criteria" in {
    val mgr1: MediaManager = new Object with MediaManagerTrait
    mgr1.addMovies(m1, m2, m3, m4)

    doSearch("NOT-FOUND", mgr1) shouldEqual List()
  }


}