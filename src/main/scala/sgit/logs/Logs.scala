package sgit.logs

import sgit.changes.Differences
import sgit.io.{CommitManipulation, ConsoleOutput, FileManipulation}
import sgit.objects.{Blob, CommitObject, StagedFile}

import scala.annotation.tailrec

object Logs {

  /**
   * Shows the differences between each commit.
   * If the previous commit is absent, the files are displayed as added.
   * @param commit The commit to check
   */
  def showDiffs(commit: CommitObject): Unit = {
    if(commit.parents.nonEmpty) {
      val lastCommitFiles: List[StagedFile] = CommitManipulation.findCommitInfos(commit.parents.head).get.files
      val lastCommitFileNames: List[String] = lastCommitFiles.map(f => f.name)
      val lastCommitSHA: List[String] = lastCommitFiles.map(f => f.shaPrint)
      commit.files
        .filter(st => lastCommitFileNames.contains(st.name) && !lastCommitSHA.contains(st.shaPrint))
        .map(f => (f, lastCommitFiles.find(prev => prev.name == f.name).get))
        .foreach(sameFiles => {
          val recent = FileManipulation.findBlob(sameFiles._1.shaPrint)
          val old = FileManipulation.findBlob(sameFiles._2.shaPrint)
          if(recent.nonEmpty && old.nonEmpty) {
            val diffs = Differences.findDifferentLines(recent.get.content, Some(old.get.content))
            ConsoleOutput.printFileDifferences(recent.get.path, Some(diffs.get._1), Some(diffs.get._2))
          }
        })
      //We remove every file that has the same name, i.e. that was already present
      val added = commit.files.filterNot(file => lastCommitFiles.exists(old => old.name == file.name))
      if(added.nonEmpty) {
        ConsoleOutput.printGreen("Added files\n")
        added.foreach(file => ConsoleOutput.printToScreen("- " + file.name))
      }
      //We remove every file that has a name in the old but not in the new, i.e. deleted files.
      val deleted = lastCommitFiles.filterNot(file => commit.files.exists(newFile => newFile.name == file.name))
      if(deleted.nonEmpty) {
        ConsoleOutput.printRed("Deleted files\n")
        deleted.foreach(file => ConsoleOutput.printToScreen("- " + file.name))
      }
    } else {
      ConsoleOutput.printGreen("Added files\n")
      commit.files.foreach(file => ConsoleOutput.printToScreen("- " + file.name))
    }
  }

  /**
   * Prints the commit infos.
   * @param commitHash the commit to print.
   */
  @tailrec
  def printCommitInfo(commitHash: String, overtime: Boolean): Unit = {
    if(!commitHash.isEmpty) {
      val commit = CommitManipulation.findCommitInfos(commitHash)
      ConsoleOutput.printYellow("Commit name: " + commit.get.name)
      ConsoleOutput.printToScreen("Date: " + commit.get.time.toString)
      ConsoleOutput.printToScreen("\n" + commit.get.desc + "\n")
      if(overtime) showDiffs(commit.get)
      if(commit.get.parents.nonEmpty) printCommitInfo(commit.get.parents.head, overtime)
    }
  }

  /**
   * Shows the logs of the commits.
   * @param overtime a parameter to show the diff between the files.
   * @param stat a parameter to show the stats for each commit.
   */
  def showLog(overtime: Boolean, stat: Boolean): Unit = {
    val lastCommit: Option[String] = CommitManipulation.findMostRecentCommit()
    if(lastCommit.isEmpty) ConsoleOutput.printError("No commits yet, nothing to show.")
    else printCommitInfo(lastCommit.get, overtime)
  }
}
