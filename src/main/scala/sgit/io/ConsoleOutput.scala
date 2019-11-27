package sgit.io

import better.files.File

object ConsoleOutput {
  /**
   * Prints to the screen.
   * @param toPrint The line to print
   */
  def printToScreen(toPrint: String): Unit = println(toPrint)

  /**
   * Prints an error message to the screen.
   * @param errorMsg the error message
   */
  def printError(errorMsg: String): Unit = System.err.println(errorMsg)

  /**
   * Prints a green message to the screen.
   * @param toPrint the line to print
   */
  def printGreen(toPrint: String): Unit = println("\u001B[32m" + toPrint + "\u001B[0m")

  /**
   * Prints a red message to the screen.
   * @param toPrint the line to print
   */
  def printRed(toPrint: String): Unit = println("\u001B[31m"+ toPrint + "\u001B[0m")

  /**
   * Prints a yellow message to the screen.
   * @param toPrint the line to print
   */
  def printYellow(toPrint: String): Unit = println("\u001B[33m" + toPrint +  "\u001B[0m")

  /**
   * Prints the untracked files.
   * @param lines the files to print
   */
  def printUntrackedFiles(lines: Seq[File]): Unit = {
    printToScreen("Untracked files:\n  (Use sgit add <file>... to add those files to the stage)")
    lines.foreach(file => printYellow("   untracked: " + FileManipulation.relativizeFilePath(file).get))
  }

  /**
   * Prints the changes to be committed.
   * @param lines the lines to print
   */
  def printChangesToBeCommitted(lines: Seq[String]): Unit = {
    printToScreen("Changes to be committed:\n  (use sgit commit to commit them)")
    lines.foreach(l => printGreen(l))
  }

  /**
   * Prints the changes not staged.
   * @param lines the lines to print
   */
  def printChangesNotStaged(lines: Seq[String]): Unit = {
    printToScreen("Changed not staged for commit:\n (Use sgit add <file>... to update what will be committed)")
    lines.foreach(l => printRed(l))
  }
}
