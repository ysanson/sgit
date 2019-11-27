package sgit.changes

import better.files._
import sgit.io.{CommitManipulation, ConsoleOutput, FileManipulation, StageManipulation}

import scala.annotation.tailrec

object Differences {

  /**
   * Finds the differences between two files' contents.
   * It does not return the actual line where the changed are.
   * @param firstFileContent the first file content.
   * @param secondFileContent the second file content, or none if it's absent.
   * @return An optional tuple of sequences of strings, the first one containing the added lines and the second one the deleted lines.
   */
  def findDifferentLines(firstFileContent: String, secondFileContent: String): Option[(Seq[String], Seq[String])] = {
    val firstFileLines = firstFileContent.split("\n").toIndexedSeq
      val secondFileLines = secondFileContent.split("\n").toIndexedSeq
      val addedLines = firstFileLines.diff(secondFileLines)
      val deletedLines = secondFileLines.diff(firstFileLines)
      if(addedLines.isEmpty && deletedLines.isEmpty) None
      else Some(addedLines, deletedLines)
  }

  /**
   * Finds the different lines for each given files.
   * @param files the input files
   * @return For each files, the added lines and the deleted ones.
   */
  def findLinesForFiles(files: Seq[(File, File)]): Seq[(Seq[String], Seq[String])] = {
    @tailrec
    def internal(files: Seq[(File, File)], diffs: Seq[(Seq[String], Seq[String])]): Seq[(Seq[String], Seq[String])] = {
      if(files.isEmpty) diffs
      else {
        val file = files.head
        val lines = findDifferentLines(file._1.contentAsString, file._2.contentAsString)
        if(lines.nonEmpty) internal(files.tail, diffs:+lines.get)
        else internal(files.tail, diffs)
      }
    }
    internal(files, Seq())
  }

  /**
   * Retrieves the stored files for every file on input.
   * @param differentFiles The files we want to retrieve their counterparts.
   * @return A list of tuples, the files in the WD and the ones in sgit.
   */
  def retrieveFiles(differentFiles: Seq[File]) : Seq[(File, File)] = {
    val committedFiles = CommitManipulation.findCommitInfos(CommitManipulation.findMostRecentCommit().get).get.files
    val stagedFiles = StageManipulation.retrieveStagedFiles()
    val storedFiles = if(stagedFiles.nonEmpty) committedFiles.concat(stagedFiles.get) else committedFiles
    differentFiles.map(file => {
      val storedFile = storedFiles.find(sf => sf.name.contains(file.name)).get
      (file, FileManipulation.retrieveFileFromObjects(storedFile.shaPrint).get)
    })
  }
  /**
   * Finds the different files between the last commit and the working dir.
   * @param baseFolder the base folder
   * @return a optional list of files
   */
  def findDifferentFilesFromCommit(baseFolder: File): Option[Seq[File]] = {
    @tailrec
    def getDifferentFiles(objects: Seq[File], shaToCheck: Seq[String], diff: Seq[File]): Seq[File] = {
      if (objects.isEmpty) diff
      else if(objects.length == 1) {
        if(shaToCheck.contains(objects.head.sha1)) diff
        else diff :+ objects.head
      }
      else {
        val currentFile: File = objects.head
        if (shaToCheck.contains(currentFile.sha1)) getDifferentFiles(objects.tail, shaToCheck, diff)
        else getDifferentFiles(objects.tail, shaToCheck.tail, diff :+ currentFile)
      }
    }
    if (!baseFolder.isDirectory) return None
    val content: Option[List[File]] = FileManipulation.findAllFiles(baseFolder)
    val lastCommit = CommitManipulation.findMostRecentCommit()
    if(lastCommit.isEmpty) None
    else {
      val committedFiles = CommitManipulation.findCommitInfos(lastCommit.get).get.files
      if (content.isEmpty || committedFiles.isEmpty) None
      else {
        val stagedFiles = StageManipulation.retrieveStagedFiles()
        val storedFiles = if(stagedFiles.nonEmpty) committedFiles.concat(stagedFiles.get) else committedFiles
        val diff = getDifferentFiles(content.get, storedFiles.map(file => file.shaPrint), Seq())
        if (diff.isEmpty) None
        else Some(diff)
      }
    }
  }

  /**
   * Finds the differences between two files.
   */
  def differences(): Unit = {
    if(!".sgit".toFile.exists) ConsoleOutput.printError("sgit have not been initialized. Please run sgit init.")
    else if(CommitManipulation.findMostRecentCommit().isEmpty) ConsoleOutput.printToScreen("No commits yet.")
    else {
      val files = findDifferentFilesFromCommit(".".toFile)
      if(files.nonEmpty) {
        retrieveFiles(files.get).foreach(f => {
          val storedFileContent = f._2.contentAsString.substring(f._2.contentAsString.indexOf("\n"))
          val differences = findDifferentLines(f._1.contentAsString, storedFileContent)
          if(differences.nonEmpty) ConsoleOutput.printFileDifferences(f._1.name, Some(differences.get._1), Some(differences.get._2))
        })
      } else {
        ConsoleOutput.printToScreen("No files modified, nothing to show.")
      }
    }
  }
}
