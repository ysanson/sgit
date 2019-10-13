package sgit.changes

import better.files._
import sgit.io.{CommitManipulation, ConsoleOutput, FileManipulation, FolderManipulation, RefManipulation, StageManipulation, TreeManipulation}
import sgit.objects.{Blob, CommitObject, Conversions, Folder, StagedFile, TreeObject}

import scala.annotation.tailrec

object Status {

  /**
   * Finds the different files between the stage and the working dir.
   *
   * @param baseFolder the base folder
   * @return a optional list of files
   */
  def findDifferentFilesFromStage(baseFolder: File): Option[Seq[File]] = {
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
    val stagedFiles = StageManipulation.retrieveStagedFiles()
    if (content.isEmpty || stagedFiles.isEmpty) None
    else {
      val diff = getDifferentFiles(content.get, stagedFiles.get.map(file => file.shaPrint), Seq())
      if (diff.isEmpty) None
      else Some(diff)
    }
  }

  /**
   * Lists the files ready to be committed.
   * if no commit, all files are listed as added.
   *
   * @param stagedFiles    the staged files
   * @param committedFiles the committed files, or none if there is no commit.
   * @return a sequence of strings to print.
   */
  def readyToBeCommitted(stagedFiles: Option[Seq[StagedFile]], committedFiles: Option[Seq[StagedFile]]): Option[Seq[String]] = {
    if (stagedFiles.isEmpty) None
    else if (committedFiles.isEmpty) {
      Some(stagedFiles.get.map(file => "   - added: " + file.name))
    } else {
      val committed = Conversions.nameAndShaFromStageFiles(committedFiles.get)
      //The name is not in the committed files
      val addedFiles: Seq[StagedFile] = stagedFiles.get.filterNot(file => committed._1.contains(file.name))
      //The name is in the committed files but the sha print isn't
      val modifiedFiles: Seq[StagedFile] = stagedFiles.get.filter(file => committed._1.contains(file.name) && !committed._2.contains(file.shaPrint))
      //Delete: ?
      Some(addedFiles.map(file => "   - added: " + file.name).concat(modifiedFiles.map(file => "   - modified: " + file.name)))
    }
  }

  /**
   * Finds the deleted files in the working directory.
   * @param workingDir the working directory
   * @param filesInObject the files in object.
   * @return
   */
  def findDeletedFiles(workingDir: Seq[File], filesInObject: Option[Seq[StagedFile]]): Option[Seq[StagedFile]] = {
    if(filesInObject.isEmpty) return None
    val filesNames: Seq[String] = workingDir.map(f => FileManipulation.relativizeFilePath(f).get)
    val del = filesInObject.get.filterNot(f => filesNames.contains(f.name))
    if(del.isEmpty) None
    else Some(del)
  }

  /**
   * Gets the files that are not staged for commit, by comparing the files from the working dir with the
   * staged and the committed (stored) ones.
   *
   * @param notInObjects   the files that are not in object (the modified and the untracked)
   * @param committedFiles the committed files
   * @param stagedFiles    the staged files
   * @return a tuple of sequences. The first is the lines to print, the second is the untracked files
   */
  def notStagedForCommit(notInObjects: Seq[File], committedFiles: Option[Seq[StagedFile]], stagedFiles: Option[Seq[StagedFile]]): (Option[Seq[String]], Seq[File]) = {

    def calculateDiff(notInObjects: Seq[File], files: Seq[StagedFile]): (Seq[String], Seq[File]) = {
      val namesAndSha = Conversions.nameAndShaFromStageFiles(files)
      val modified: Seq[File] = notInObjects.filter(file =>
        namesAndSha._1.contains(FileManipulation.relativizeFilePath(file).get) && !namesAndSha._2.contains(file.sha1))
      val added: Seq[File] = notInObjects
        .filter(file => namesAndSha._1.contains(FileManipulation.relativizeFilePath(file).get))
        .diff(modified)
      val res1 = added
        .map(file => "   - added: " + FileManipulation.relativizeFilePath(file).get)
        .concat(modified.map(file => "   - modified: " + FileManipulation.relativizeFilePath(file).get))
      (res1, notInObjects.diff(added).diff(modified))
    }

    if (committedFiles.isEmpty && stagedFiles.isEmpty) {
      (None, notInObjects)
    } else if(committedFiles.nonEmpty && stagedFiles.isEmpty) {
      val res = calculateDiff(notInObjects, committedFiles.get)
      if(res._1.isEmpty) (None, res._2)
      else (Some(res._1), res._2)
    }else if(committedFiles.isEmpty && stagedFiles.nonEmpty) {
      val res = calculateDiff(notInObjects, stagedFiles.get)
      if(res._1.isEmpty) (None, res._2)
      else (Some(res._1), res._2)
    }else {
      val stage = calculateDiff(notInObjects, stagedFiles.get)
      val commit = calculateDiff(notInObjects, committedFiles.get)
      val files = stage._1.map(line =>
       line.split(" ").last.toFile)
        .concat(commit._1.map(line =>
        line.split(" ").last.toFile))
      (Some(stage._1.concat(commit._1).distinct), stage._2.concat(commit._2).distinct.diff(files))
    }
  }

  /**
   * Prints the status of the sgit.
   */
  def status(): Unit = {
    if (!".sgit".toFile.exists) ConsoleOutput.printError("sgit is not initialized. Please run sgit init.")
    else {
      ConsoleOutput.printToScreen("On branch " + RefManipulation.getBranchName)

      val rootDir: File = ".sgit".toFile.parent
      val stageDiff: Option[Seq[StagedFile]] = StageManipulation.retrieveStagedFiles()
      val allFiles: List[File] = FolderManipulation.listAllChildren(rootDir).get.filterNot(file => file.isDirectory)
      val notInObjects: Seq[File] = FileManipulation.searchUntrackedFiles(allFiles)

      val commit = CommitManipulation.findCommitInfos(CommitManipulation.findMostRecentCommit().getOrElse("null"))
      if(commit.isEmpty) ConsoleOutput.printToScreen("No commit yet\n\n")
      else ConsoleOutput.printToScreen("Last commit is " + commit.get.name + "\n\n")

      if (commit.isEmpty && stageDiff.isEmpty) ConsoleOutput.printUntrackedFiles(notInObjects)
      else {
        val deletedFiles: Option[Seq[StagedFile]] = findDeletedFiles(allFiles, if(commit.isEmpty) None else Some(commit.get.files))
        val commitFiles: Option[Seq[StagedFile]] = if(commit.isEmpty) None else Some(commit.get.files)
        val tbc = readyToBeCommitted(stageDiff, commitFiles)
        val nsfc = notStagedForCommit(notInObjects, commitFiles, stageDiff)
        if (tbc.nonEmpty)
          ConsoleOutput.printChangedToBeCommitted(tbc.get)
        println(deletedFiles)
        if (nsfc._1.nonEmpty && deletedFiles.isEmpty)
          ConsoleOutput.printChangesNotStaged(nsfc._1.get)
        else if(nsfc._1.nonEmpty && deletedFiles.nonEmpty) {
          val deleted: Seq[String] = deletedFiles.get.map(f => "   - deleted: " + f.name)
          ConsoleOutput.printChangesNotStaged(nsfc._1.get.concat(deleted))
        }
        else if(nsfc._1.isEmpty && deletedFiles.nonEmpty)
          ConsoleOutput.printChangesNotStaged(deletedFiles.get.map(f => "   - deleted: " + f.name))
        if (nsfc._2.nonEmpty)
          ConsoleOutput.printUntrackedFiles(nsfc._2)
      }
    }
  }
}
