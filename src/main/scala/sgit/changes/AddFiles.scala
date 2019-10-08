package sgit.changes

import java.io.{File => JFile}

import better.files._
import sgit.io.{ConsoleOutput, FileManipulation, StageManipulation}
import sgit.objects.{Blob, StagedFile}

import scala.annotation.tailrec

object AddFiles {
  /**
   * Searches for new files in the directory.
   * @return a sequence of files.
   */
  def searchForNewFiles(): Seq[StagedFile] = {
    StageManipulation.retrieveStagedFiles().get
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
        if(firstFile.getPath.contains(".sgit")) return res
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

  def deleteDuplicatedStagedFiles(): Unit = {
    val stagedFiles: Option[Seq[StagedFile]] = StageManipulation.retrieveStagedFiles()
    if(stagedFiles.isDefined){

      def findMostRecentFile(files: Seq[(String, File)]): Seq[File] = {
        val f: Seq[File] = files.map(v => v._2)
        f.filter(file => file != f.max(File.Order.byModificationTime))
      }

      val duplicates: Seq[String] = stagedFiles.get.map(f => f.name)
      println("dup1: " + duplicates)
      val singleDup: Seq[String] = duplicates.intersect(duplicates.distinct).distinct
      println("dup2: " + singleDup)
      val dupFiles: Seq[StagedFile] = stagedFiles.get.filter(f => singleDup.contains(f.name))
      val filesToDelete: Seq[File] = dupFiles
        .map(file => {
          val stored = FileManipulation.retrieveFileFromObjects(file.shaPrint).orNull
          (file.name, stored)
        })
        .groupBy(v => v._1)
        .flatMap((group: (String, Seq[(String, File)])) => findMostRecentFile(group._2)).toSeq
      val shaToDelete: Seq[String] = filesToDelete.map(file => file.name) //Because it's the stored files (ie the names are the sha1 keys)
      StageManipulation.removeFilesFromStaged(shaToDelete)
      shaToDelete.foreach(sha => FileManipulation.removeFileFromObjects(sha))
    }
  }

  /**
   * Adds certain files to the stage.
   * @param files the files given by the user.
   */
  def add(files: Seq[JFile]): Unit = {
    val addedFiles: Option[Seq[Blob]] = FileManipulation.addFilesToObjects(getFiles(files))
    if(addedFiles.isEmpty)
      ConsoleOutput.printToScreen("sgit had not been initialized. Please run sgit init.")
    else {
      val staged: Option[Boolean] = StageManipulation.addFilesToStaged(addedFiles.get)

      if(staged.isEmpty)
        ConsoleOutput.printToScreen("Cannot add the files to the stage.")
      else {
        deleteDuplicatedStagedFiles()
        ConsoleOutput.printToScreen("Successfully added files to the stage!")
      }
    }
  }
}
