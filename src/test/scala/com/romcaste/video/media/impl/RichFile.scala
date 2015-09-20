package com.romcaste.video.media.impl

import java.io._
import scala.io._

class RichFile( file: File ) {

  def text = Source.fromFile( file )(Codec.UTF8).mkString

  def text_=( s: String ) {
    val out = new PrintWriter( file , "UTF-8")
    try{ out.print( s ) }
    finally{ out.close }
  }
}

object RichFile {

  implicit def enrichFile( file: File ) = new RichFile( file )

}
