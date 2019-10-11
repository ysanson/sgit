package sgit.io

import better.files.File

object ConsoleOutput {
  def printToScreen(toPrint: String): Unit = println(toPrint)

  def printError(errorMsg: String): Unit = System.err.println(errorMsg)

  def printGreen(toPrint: String): Unit = println("\u001B[32m" + toPrint + "\u001B[0m")

  def printRed(toPrint: String): Unit = println("\u001B[31m"+ toPrint + "\u001B[0m")

  def printYellow(toPrint: String): Unit = println("\u001B[33m" + toPrint +  "\u001B[0m")

  def printUntrackedFiles(lines: Seq[File]): Unit = {
    printToScreen("Untracked files:\n  (Use sgit add <file>... to add those files to the stage)")
    lines.foreach(file => printYellow("   untracked: " + FileManipulation.relativizeFilePath(file).get))
  }

  def printChangedToBeCommitted(lines: Seq[String]): Unit = {
    printToScreen("Changes to be committed:\n  (use sgit commit --message to commit them)")
    lines.foreach(l => printGreen(l))
  }

  def printChangesNotStaged(lines: Seq[String]): Unit = {
    printToScreen("Changed not staged for commit:\n (Use sgit add <file>... to update what will be committed)")
    lines.foreach(l => printRed(l))
  }
}
