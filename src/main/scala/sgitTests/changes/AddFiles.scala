package sgitTests.changes

import java.io.{File => JFile}

import better.files._
import sgitTests.io.{ConsoleOutput, FileManipulation, StageManipulation}
import sgitTests.objects.{Blob, StagedFile, Conversions}

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
        if(firstFile.getPath.contains(".sgit")) findFiles(files.tail, res)
        else {
          if(firstFile.isDirectory)
            findFiles(files.tail, res ++ listFolder(firstFile))
          else {
            if(firstFile.exists()) findFiles(files.tail, res:+firstFile.toString.toFile)
            else findFiles(files.tail, res)
          }
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
   * Finds the duplicated files between the old stage and the new stage.
   * If the sha prints are different but the name is the same, it removes the old file from the final stage
   * @param addedFiles The new files from the most recent add call.
   * @param existingFiles the existing files in the stage.
   * @return a tupe of sequence of files. _1 contains the new stage, _2 contains the files to delete.
   */
  def findDuplicatedStagedFiles(addedFiles: Seq[StagedFile], existingFiles: Seq[StagedFile]): (Seq[StagedFile], Seq[StagedFile]) = {
    if(existingFiles == null || existingFiles.isEmpty) return (addedFiles, Seq())

    val addedNames: Seq[String] = addedFiles.map(f => f.name)
    val addedSHA: Seq[String] = addedFiles.map(f => f.shaPrint)
    //We keep the files that are duplicated but that don't have the same SHA than the added files.
    val filesToDelete: Seq[StagedFile] =
      existingFiles.filter(old => addedNames.contains(old.name) && !addedSHA.contains(old.shaPrint))

    (addedFiles.concat(existingFiles.diff(filesToDelete)), filesToDelete)
  }

  /**
   * Adds certain files to the stage.
   * This method is not RT nor pure
   * @param files the files given by the user.
   */
  def add(files: Seq[JFile]): Unit = {
    val addedFiles: Option[Seq[Blob]] = FileManipulation.addFilesToObjects(getFiles(files)) //Has side effects
    if(addedFiles.isEmpty)
      ConsoleOutput.printError("sgit had not been initialized. Please run sgit init.")

    else {
      val existingStagedFiles = StageManipulation.retrieveStagedFiles().orNull
      val contentAndOlds: (Seq[StagedFile], Seq[StagedFile]) =
        findDuplicatedStagedFiles(Conversions.convertBlobsToStagedFiles(addedFiles.get), existingStagedFiles)

      if(contentAndOlds._2.nonEmpty) {
        @tailrec
        def deleteObjects(files: Seq[StagedFile]): Unit = {
          if(files.isEmpty) return
          FileManipulation.removeFileFromObjects(files.head.shaPrint)
          deleteObjects(files.tail)
        }
        deleteObjects(contentAndOlds._2)
      }
      val staged: Option[Boolean] = StageManipulation.addFilesToStaged(contentAndOlds._1) //Has side effects

      if(staged.isEmpty)
        ConsoleOutput.printToScreen("Cannot add the files to the stage.")
      else {
        ConsoleOutput.printToScreen("Successfully added files to the stage!")
      }
    }
  }
}
