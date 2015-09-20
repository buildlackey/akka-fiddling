package com.romcaste.video.media.impl

import java.io.InputStream
import java.util.NoSuchElementException

import com.romcaste.video.media.MediaException

import scala.io.Source._

import java.io.FileInputStream

/**
 * Parses an ASCII input stream into a list of records, each of which is itself a list of String fields.
 * Ensures that any passed in input stream is properly closed.
 *
 * The requirements for the format are as follows:
 *
 * <pre>
 * 1. The list of records SHALL only contain base ASCII characters
 * 2. Each record SHALL be separated by a single newline (one record per line)
 * 3. Each field within a record shall be separated by a single pipe "|" character
 * 4. The fields of a movie will appear in the following order:
 * </pre>
 *
 * <p>In addition, each of the special characters may be escaped and used as part of the field
 * instead of a delimiter if it is immediately preceded with a forward slash (<code>\</code>).
 * For example, explicit newlines within a records's description will appear as the string <code>
 * "\n"</code> and the field:  <code>"Escape | A Made Up Movie"</code> would appear as
 * <code>"Escape \| A Made Up Movie"</code>.</p>
 *
 * Mutable: Not thread safe.
 *
 */
private[impl] class InputStreamRecordParser(stream: InputStream, numFieldsPerLine: Int, delimiter: Char) {
  private[this] var getRecordsCalled = false


  def getRecords = new Iterator[Seq[String]] {
    if (getRecordsCalled)
      throw new MediaException("getRecords can only be called once an instance of " + this.getClass.getName )
    else
       getRecordsCalled = true

    private[this] val lines: Iterator[String] = fromInputStream(stream).getLines()

    def hasNext = lines.hasNext

    def next() = {
      if (!hasNext) {
        throw new MediaException(" call to next() while iterator exhausted")
      } else {
        parseFields(lines.next())
      }
    }

    def containsNonBaseAsciiChar(str: String) = str exists ((c: Char) => c >= 128) // extended ASCII chars are >= 128

    def parseFields(str: String): Seq[String] = {
      if (containsNonBaseAsciiChar(str)) {
        throw new MediaException("failed to parse string containing non base ascii badChar: " + str)
      }

      val fakePipe = "" + 230.toChar // non base char that will help us split fields with escaped pipe
      val fakeNewline = "" + 240.toChar // non base char that will help us split fields with escaped newlines
      val safeDelimiter = "" + 250.toChar // some delimiters like '|' cause trouble 'cause they are regex chars
      val quarantineSpecialChars =
        str.replace('\\' + "n", fakeNewline).
          replace('\\' + "|", fakePipe).
          replace(delimiter + "", safeDelimiter)
      val result =
        quarantineSpecialChars. split(safeDelimiter+"").      // after split, get an array of strings...
          map(_.replace(fakeNewline, "\n").replace(fakePipe, "|").replace(safeDelimiter,""+delimiter)).toSeq
      if (result.length != numFieldsPerLine) {
        throw new MediaException(s"expected $numFieldsPerLine fields, but got ${result.length} in line: $str")
      }
      result
    }
  }
}


private[impl] object InputStreamRecordParser {
  val default = '|'

  def using[T](stream: InputStream, numFieldsPerLine: Int, delimiter: Char)
              (func: InputStreamRecordParser => T): T =
    try {
      val parser = new InputStreamRecordParser(stream, numFieldsPerLine, delimiter)
      func(parser)
    } finally {
      stream.close()
    }


  def using[T](file: java.io.File, numFieldsPerLine: Int, delimiter: Char)
              (f: InputStreamRecordParser => T): T =
    using(new FileInputStream(file), numFieldsPerLine, delimiter)(f)
}
