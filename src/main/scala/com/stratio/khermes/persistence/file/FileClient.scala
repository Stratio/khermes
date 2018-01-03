package com.stratio.khermes.persistence.file

import com.typesafe.scalalogging.LazyLogging

import scala.util.Success

/**
  * Created by Emiliano Martinez on 2/11/17.
  * Class to write lines in a synchronous way
  */
class FileClient[K](path: String)
    extends LazyLogging {

  import java.io.BufferedWriter
  import java.io.FileWriter
  import java.io.IOException
  var bw: BufferedWriter = null

  def send(message: String): Unit = {
    try {
      bw = new BufferedWriter(new FileWriter(s"$path", true))
      if(!message.isEmpty) {
        bw.write(message.trim() + "\n")
        bw.flush()
      }
    } catch {
      case ioe: IOException =>
        ioe.printStackTrace()
    } finally {
      if (bw != null)
        try bw.close()
        catch {
          case ioe2: IOException =>
        }
    }
  }
}







