package com.romcaste.video.media.impl

import java.io.{ByteArrayInputStream, File}

import com.romcaste.video.media.MediaException
import com.romcaste.video.movie.{MediaType, Rating}
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, ShouldMatchers}

import scala.language.postfixOps


// should handle   foo\\nBar  as a string with a backslash followed by a newline, giving two separate lines"
// should interpret a lone backslash   \  as just a backslash.
// should interpret the line "foo\||bar"  as two fields: 'foo|' and 'bar'
// should interpret the line "foo\||\|bar"  as two fields: 'foo|' and '|bar'

@RunWith(classOf[JUnitRunner])
class InputStreamRecordParserImplTest extends FlatSpec with ShouldMatchers {

  "An  InputStreamRecordParserImpl" should "raise expected error on a file containing non-ascii characters" in {
    intercept[MediaException] {
      val inputBytes = ("" + 241.toChar).getBytes
      new InputStreamRecordParser(new ByteArrayInputStream(inputBytes), 1, 'x').getRecords.next()
    }
  }
  it should "raise an exception if a line does not contain the expected number of fields" in {
    val inputBytes = "foo|bar".getBytes
    new InputStreamRecordParser(new ByteArrayInputStream(inputBytes), 2, '|').getRecords.next() // 2: no error

    intercept[MediaException] {
      new InputStreamRecordParser(new ByteArrayInputStream(inputBytes), 1, '|').getRecords.next() // 1:  triggers error
    }
  }
  it should "treat escaped special character 'newline' as field text instead of a delimiter " in {
    val inputBytes = "first-with-escaped-newline\\n-followedBy*second".getBytes
    val parser: InputStreamRecordParser = new InputStreamRecordParser(new ByteArrayInputStream(inputBytes), 2, '*')
    val firstRecord = parser.getRecords.next
    firstRecord.size shouldEqual 2

    val firstField: String = firstRecord(0)
    val secondField: String = firstRecord(1)

    firstField should include ("first")
    firstField.split("\n").length shouldEqual 2     // first field should have a real newline in it
    secondField should include ("second")
  }
  it should "treat escaped special character '|' as field text instead of a delimiter " in {
    val inputBytes = "first-with-escaped-pipe\\|-followedBy*second".getBytes
    val parser: InputStreamRecordParser = new InputStreamRecordParser(new ByteArrayInputStream(inputBytes), 2, '*')
    val firstRecord = parser.getRecords.next
    firstRecord.size shouldEqual 2

    val firstField: String = firstRecord(0)
    val secondField: String = firstRecord(1)

    firstField should include ("first")
    firstField should include ("|")
    secondField should include ("second")
  }
  it should "not allow getRecords to be called twice" in {
    val inputBytes = "first|second".getBytes
    val parser: InputStreamRecordParser = new InputStreamRecordParser(new ByteArrayInputStream(inputBytes), 2, '|')
    parser.getRecords
    intercept[MediaException] {
      parser.getRecords
    }
  }
}
