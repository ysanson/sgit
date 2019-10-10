package sgit.changes

import better.files._
import sgit.io.{CommitManipulation, ConsoleOutput, FileManipulation, FolderManipulation, StageManipulation, TreeManipulation}
import sgit.objects.{Blob, Commit, Conversions, Folder, StagedFile, TreeObject}

import scala.annotation.tailrec

object Differences {
  /**
   * Searches for the files that have been modified, added or deleted.
   * It starts from a base folder and searches all the way through.
   * It only works by comparing to the most recent commit.
   * @param baseFolder the base folder to search from
   * @return a sequence of files that have been modified, or None if error.
   */
  def findDifferentFilesFromCommit(baseFolder: File): Option[Seq[File]] = {
    if (!baseFolder.isDirectory) return None

    val content: Option[List[File]] = FolderManipulation.listSubDirectories(baseFolder)
    if (content.isEmpty) return None
    println(content)
    val commit: Option[String] = CommitManipulation.findMostRecentCommit()

    if (commit.isEmpty) return None

    val currentCommit: Commit = CommitManipulation.findCommitInfos(commit.get).get
    //TODO: Change the implementation
    /*val tree: TreeObject = TreeManipulation.extractTreeFromCommit(currentCommit)

    val diffs = content.get
      .flatMap(file => {
        val obj: Option[TreeObject] =
          TreeManipulation.searchInTree(tree, FileManipulation.relativizeFilePath(file).get)
        if (obj.isEmpty) return null
        obj.get match {
          case _: Blob => null
          case folder: Folder => FolderManipulation.findDifferentFilesForFolder(file, folder).orNull
        }
      })
      .filterNot(file => file == null)
    Some(diffs)*/
    None
  }

  /**
   * Finds the different files between the stage and the working dir.
   * @param baseFolder the base folder
   * @return a optional list of files
   */
  def findDifferentFilesFromStage(baseFolder: File): Option[Seq[File]] = {
    @tailrec
    def getDifferentFiles(objects: Seq[File], shaToCheck: Seq[String], diff: Seq[File]): Seq[File] = {
      if(objects.isEmpty) diff
      else {
        val currentFile: File =  objects.head
        if(shaToCheck.contains(currentFile.sha1))
          getDifferentFiles(objects.tail, shaToCheck, diff)
        else
          getDifferentFiles(objects.tail, shaToCheck.tail, diff:+currentFile)
      }
    }
    if (!baseFolder.isDirectory) return None
    val content: Option[List[File]] = FolderManipulation.listAllChildren(baseFolder)
    val stagedFiles = StageManipulation.retrieveStagedFiles()
    if (content.isEmpty && stagedFiles.isEmpty) return None
    val diff = getDifferentFiles(content.get, stagedFiles.get.map(file => file.shaPrint), Seq())
    if(diff.isEmpty) None
    else Some(diff)
  }

  /**
   * Prints the status of the sgit.
   */
  def status(): Unit = {
    if(!".sgit".toFile.exists) ConsoleOutput.printError("sgit is not initialized. Please run sgit init.")
    else {
      val rootDir: File = ".sgit".toFile.parent
      val stageDiff: Option[Seq[File]] = findDifferentFilesFromStage(rootDir)
      if(stageDiff.isEmpty) return
      else {
        val allFiles = rootDir.listRecursively.toIndexedSeq:+rootDir
        val untracked: Seq[File] = FileManipulation.searchUntrackedFiles(allFiles)
        val diffFromStage = findDifferentFilesFromStage(rootDir)
        if(diffFromStage.isEmpty) return
        val supp = diffFromStage.get.diff(allFiles)
        val add = allFiles.diff(diffFromStage.get)
        ConsoleOutput.printToScreen("Untracked files: ")
        untracked.foreach(file => ConsoleOutput.printToScreen("untracked- " + FileManipulation.relativizeFilePath(file)))
        ConsoleOutput.printToScreen("\nDeleted files: ")
        supp.foreach(file => ConsoleOutput.printToScreen("deleted- " + FileManipulation.relativizeFilePath(file)))
        ConsoleOutput.printToScreen("\nAdded files: ")
        add.foreach(file => ConsoleOutput.printToScreen("added- " + FileManipulation.relativizeFilePath(file)))
      }
    }
  }
}
