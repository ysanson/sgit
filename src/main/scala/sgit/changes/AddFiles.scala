package sgit.changes

import java.io.{File => JFile}
import better.files._
import sgit.io.{FileManipulation, ConsoleOutput}

object AddFiles {
  def searchForNewFiles(): Seq[File] = {
    FileManipulation.retrieveStagedFiles()
  }

  def getFiles(files: Seq[JFile]): Seq[File] = {
    if(files.head.getName == '.') searchForNewFiles()
    else
      files.flatMap(file => FileManipulation.getFile(file.getPath))
  }

  def add(files: Seq[JFile]): Unit = {
    val addedFiles: Option[Seq[String]] = FileManipulation.addFilesToObjects(getFiles(files))
    if(addedFiles.isEmpty)
      ConsoleOutput.printToScreen("sgit had not been initialized. Please run sgit init.")
    else {
      val staged = FileManipulation.addFilesToStaged(addedFiles.get)
      if(staged.isEmpty)
        ConsoleOutput.printToScreen("Cannot add the files to the stage.")
      else
        ConsoleOutput.printToScreen("Successfully added files to the stage!")
    }
  }
}
