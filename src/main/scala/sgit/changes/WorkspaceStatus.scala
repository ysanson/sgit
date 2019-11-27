package sgit.changes

import better.files._
import sgit.io.{CommitManipulation, ConsoleOutput, FileManipulation, FolderManipulation, RefManipulation, StageManipulation, TreeManipulation}
import sgit.objects.{Blob, CommitObject, Conversions, Folder, StagedFile, TreeObject}

import scala.annotation.tailrec

object WorkspaceStatus {

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
    else if (committedFiles.isEmpty) Some(stagedFiles.get.map(file => "   - added: " + file.name))
    else {
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
   * Finds the files that are not in the branch (possibly present in the objects folder but not in the last commit or stage)
   * @param files the files to check
   * @param storedFiles the stored files
   * @return the files that are not in the current branch.
   */
  def findFilesNotInBranch(files: Seq[File], storedFiles: Seq[StagedFile]): Seq[File] = {
    val storedSha = storedFiles.map(store => store.shaPrint)
    files.filterNot(file => storedSha.contains(file.sha1))
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
      val commit = CommitManipulation.findMostRecentCommit()
      if(commit.isEmpty) ConsoleOutput.printToScreen("No commit yet\n\n")
      else ConsoleOutput.printToScreen("Last commit is " + commit.get + "\n\n")

      if (commit.isEmpty && stageDiff.isEmpty) ConsoleOutput.printUntrackedFiles(notInObjects)
      else if(commit.isEmpty && stageDiff.nonEmpty) {
        val tbc = readyToBeCommitted(stageDiff, None)
        ConsoleOutput.printChangesToBeCommitted(tbc.get)
      }
      else if(commit.nonEmpty && stageDiff.isEmpty) ConsoleOutput.printYellow("No staged files. Working tree clean.")
      else if(commit.nonEmpty) {
        val commitInfos = CommitManipulation.findCommitInfos(commit.get)
        val notInCurrentBranch = findFilesNotInBranch(allFiles, if(stageDiff.nonEmpty) commitInfos.get.files.concat(stageDiff.get) else commitInfos.get.files)
        val deletedFiles: Option[Seq[StagedFile]] = findDeletedFiles(allFiles, if(commit.isEmpty) None else Some(commitInfos.get.files))
        val commitFiles: Option[Seq[StagedFile]] = if(commit.isEmpty) None else Some(commitInfos.get.files)

        //Changes ready to be committed
        val tbc = readyToBeCommitted(stageDiff, commitFiles)
        //Changes that are not staged for commit. Also contains the untracked files at position 2
        val nsfc: (Option[Seq[String]], Seq[File]) = notStagedForCommit(notInCurrentBranch, commitFiles, stageDiff)
        if (tbc.nonEmpty)
          ConsoleOutput.printChangesToBeCommitted(tbc.get)
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
      } else{
        ConsoleOutput.printYellow("No commits or staged files yet, working tree clean.")
      }
    }
  }
}
