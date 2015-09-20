package com.romcaste.video.media.impl

import java.io.File
import org.scalatest._

/**
 * Creates a temporary folder for the lifetime of a single test.
 * The folder's name will exist in a `File` field named `testFolder`.
 *
 * From: https://github.com/robey/scalatest-mixins/blob/master/src/main/scala/com/twitter/scalatest/TestFolder.scala
 */
trait TempFolderTestingFixture  extends SuiteMixin { self: Suite =>
  var testFolder: File = _

  private def deleteFile(file: File) {
    if (!file.exists) return
    if (file.isFile) {
      file.delete()
    } else {
      file.listFiles().foreach(deleteFile)
      file.delete()
    }
  }

  abstract override def withFixture(test: NoArgTest) :Outcome = {
    val tempFolder = System.getProperty("java.io.tmpdir")
    var folder: File = null

    do {
      folder = new File(tempFolder, "scalatest-" + System.nanoTime)
    } while (! folder.mkdir())

    testFolder = folder

    try {
      super.withFixture(test)
    } finally {
      deleteFile(testFolder)
    }
  }
}
