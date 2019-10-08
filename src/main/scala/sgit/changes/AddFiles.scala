package sgit.changes

import java.io.{File => JFile}

import better.files._
import sgit.io.{ConsoleOutput, FileManipulation}

import scala.annotation.tailrec

object AddFiles {
  /**
   * Searches for new files in the directory.
   * @return a sequence of files.
   */
  def searchForNewFiles(): Seq[File] = {
    FileManipulation.retrieveStagedFiles()
  }

  /**
   * Gets the files from the working directory
   * @param files a sequence of Java files given by the user.
   * @return a sequence of files corresponding to the actual files.
   */
  def getFiles(files: Seq[JFile]): Seq[File] = {
      @tailrec
      def findFiles(files: Seq[JFile], res: Seq[File]): Seq[File] = {
        if(files.isEmpty) return res
        val firstFile: JFile = files.head
        if(firstFile.isDirectory)
          findFiles(files.tail, res ++ listFolder(firstFile))
        else {
          if(firstFile.exists()) findFiles(files.tail, res:+firstFile.toString.toFile)
          else findFiles(files.tail, res)
        }
      }

    def listFolder(folder: JFile): Seq[File] ={
      if(!folder.isDirectory) return null
      val contents: Seq[JFile] = folder.listFiles()
      findFiles(contents, Seq[File]())
    }
    findFiles(files, Seq[File]()).filterNot(file => file == null)
  }

  /**
   * Adds certain files to the stage.
   * @param files the files given by the user.
   */
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
