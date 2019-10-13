package sgit.logs

import sgit.io.{CommitManipulation, ConsoleOutput}

import scala.annotation.tailrec

object Logs {

  /**
   * Prints the commit infos.
   * @param commitHash the commit to print.
   */
  @tailrec
  def printCommitInfo(commitHash: String): Unit = {
    if(!commitHash.isEmpty) {
      val commit = CommitManipulation.findCommitInfos(commitHash)
      ConsoleOutput.printYellow("Commit name: " + commit.get.name)
      ConsoleOutput.printToScreen("Date: " + commit.get.time.toString)
      ConsoleOutput.printToScreen("\n" + commit.get.desc + "\n")
      if(commit.get.parents.nonEmpty) printCommitInfo(commit.get.parents.head)
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
    else printCommitInfo(lastCommit.get)
  }
}
