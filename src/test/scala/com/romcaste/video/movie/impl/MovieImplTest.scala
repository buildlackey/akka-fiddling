package com.romcaste.video.movie.impl

import com.romcaste.video.movie.{MediaType, Rating}
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, ShouldMatchers}

import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class MovieImplTest extends FlatSpec with ShouldMatchers {


  val _temporaryFolder = new TemporaryFolder

   @Rule
   def temporaryFolder = _temporaryFolder

  val movieImpl =
    new MovieImpl(
      title = "foo",
      media = MediaType.VHS,
      year = 2001,
      description = "a movie",
      actors = List("joe"),
      rating = Rating.G)

  val mSameValues =
    new MovieImpl(
      title = "foo",
      media = MediaType.VHS,
      year = 2001,
      description = "a movie",
      actors = List("joe"),
      rating = Rating.G)

  val mDifferentValues =
    new MovieImpl(
      title = "foo",
      media = MediaType.VHS,
      year = 2005,
      description = "a movie",
      actors = List("joe"),
      rating = Rating.R) // different rating

  val m2actors =
    new MovieImpl(
      title = "foo",
      media = MediaType.VHS,
      year = 2008,
      description = "a movie",
      actors = List("joe", "bob"),
      rating = Rating.G)

  val mSame2actors =
    new MovieImpl(
      title = "foo",
      media = MediaType.VHS,
      year = 2008,
      description = "a movie",
      actors = List("bob", "joe"),
      rating = Rating.G)


  "A MovieImpl" should "equal another instance with the same values" in {
    movieImpl shouldEqual mSameValues
  }
  it should "not equal an instance with different values" in {
    movieImpl should not equal mDifferentValues
  }
  it should "equal a second instance even if actors are specified in different orders" in {
    m2actors shouldEqual mSame2actors
  }
  it should "be considered to exist in maps when it's values equal those of an instance that's 'really there'" in {
    val map = Map(m2actors -> 100)
    map should contain(mSame2actors, 100) // 2 different instances hash to same value, so lookup succeeds
  }
  it should "collapse redundant specification of identical actors to one cannonical actor name" in {
    val m = new MovieImpl(
      title = "foo",
      media = MediaType.VHS,
      year = 2001,
      description = "a movie",
      actors = List("bob", "joe", "joe"),
      rating = Rating.G)
    m.getActors.toList shouldEqual List("bob", "joe")
  }
  it should "return its individual fields as requested by getField" in {
    movieImpl.getTitle shouldEqual "foo"
    movieImpl.getDescription shouldEqual "a movie"
    movieImpl.getActors shouldEqual List("joe")
    movieImpl.getYear shouldEqual 2001
    movieImpl.getRating shouldEqual Rating.G
    movieImpl.getMedia shouldEqual MediaType.VHS
  }
  it should "order movies by cast (i.e., list of actors) where those lists are treated like Comparables" in {
    MovieImpl.compareLists(Nil: List[String], Nil: List[String]) shouldBe false
    MovieImpl.compareLists(Nil: List[String], Nil: List[String]) shouldBe false
    MovieImpl.compareLists(List("fergie"), Nil: List[String]) shouldBe false
    MovieImpl.compareLists(Nil: List[String], List("fergie")) shouldBe true
    MovieImpl.compareLists(List("bob", "joe"), List("bob")) shouldBe false
    MovieImpl.compareLists(List("bob"), List("bob", "joe")) shouldBe true
    MovieImpl.compareLists(List("bob", "jo"), List("bob", "joe")) shouldBe true
  }
}