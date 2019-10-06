package sgit.changes

import java.io.{File => JFile}
import better.files._
import sgit.io.FileManipulation

object AddFiles {
  def searchForNewFiles(): Seq[File] = {
    FileManipulation.retrieveStagedFiles()
  }

  def getFiles(files: Seq[JFile]): Seq[File] = {
    if(files.head.getName == '.') searchForNewFiles()
    else
      files.iterator
        .flatMap(file => FileManipulation.findFiles(file.getPath))
        .toSeq

  }

  def add(files: Seq[JFile]): Boolean = {
    println(getFiles(files))
    true
  }
}
