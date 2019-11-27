package sgit.changes

import better.files._
import sgit.io.{CommitManipulation, ConsoleOutput, FileManipulation, FolderManipulation, StageManipulation}
import sgit.objects.Blob

import scala.annotation.tailrec

object Differences {

  /**
   * Finds the differences between two files' contents.
   * It does not return the actual line where the changed are.
   * @param firstFileContent the first file content.
   * @param secondFileContent the second file content.
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
   * Finds the different files between the last commit and the working dir.
   * @param baseFolder the base folder
   * @return a optional list of files
   */
  def findDifferentFilesFromCommit(baseFolder: File): Option[Seq[File]] = {
    @tailrec
    def getDifferentFiles(objects: Seq[File], shaToCheck: Seq[String], diff: Seq[File]): Seq[File] = {
      if (objects.isEmpty) diff
      else {
        val currentFile: File = objects.head
        if (shaToCheck.contains(currentFile.sha1))
          getDifferentFiles(objects.tail, shaToCheck, diff)
        else
          getDifferentFiles(objects.tail, shaToCheck.tail, diff :+ currentFile)
      }
    }
    if (!baseFolder.isDirectory) return None
    val content: Option[List[File]] = FolderManipulation.listAllChildren(baseFolder)
    val lastCommit = CommitManipulation.findMostRecentCommit()
    if(lastCommit.isEmpty) None
    else {
      val committedFiles = CommitManipulation.findCommitInfos(lastCommit.get).get.files
      if (content.isEmpty || committedFiles.isEmpty) None
      else {
        val diff = getDifferentFiles(content.get, committedFiles.map(file => file.shaPrint), Seq())
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
        ConsoleOutput.printError("Error: Diff is not yet implemented.")
      }
    }
  }
}
