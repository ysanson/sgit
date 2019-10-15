package sgit.changes

import sgit.io.{CommitManipulation, ConsoleOutput, FileManipulation, RefManipulation}
import sgit.objects.{Blob, StagedFile}

import scala.annotation.tailrec

object Restore {

  /**
   * Restores files in the working directory.
   * @param files the list of files to restore.
   */
  @tailrec
  def restoreFiles(files: List[StagedFile]): Unit = {
    if(files.nonEmpty) {
      val file = files.head
      val blob: Option[Blob] = FileManipulation.findBlob(file.shaPrint)
      if(blob.nonEmpty) FileManipulation.writeBlobInWorkingDir(blob.get)
      restoreFiles(files.tail)
    }
  }

  /**
   * Restores a commit
   * @param commitName the commit name
   */
  def restoreCommit(commitName: String): Boolean = {
    val commit = CommitManipulation.findCommitInfos(commitName)
    if(commit.nonEmpty) {
      restoreFiles(commit.get.files)
      true
    }
    else {
      ConsoleOutput.printError("No commit with that name!")
      false
    }
  }

  /**
   * Handles the checkout command.
   * If no name is given, checks out the last referenced commit.
   * If a name is given, searches for a branch, then for a tag with that name.
   * If a branch is checked out, it resets the HEAD to it.
   * @param name The optional name.
   */
  def checkout(name: String): Boolean = {
    val lastCommit = CommitManipulation.findMostRecentCommit()
    if(lastCommit.isEmpty) {
      ConsoleOutput.printError("No commits yet. Please commit and / or create tags/branches.")
      false
    }
    else {
      val branches = RefManipulation.getAllBranches.get
      val tags = RefManipulation.getAllTags
      if(branches.contains(name)) {
        restoreCommit(branches(name))
        RefManipulation.updateHead(name)
        true
      }
      else if(tags.nonEmpty && tags.get.contains(name)) restoreCommit(tags.get(name))
      else restoreCommit(name)
    }
  }
}
