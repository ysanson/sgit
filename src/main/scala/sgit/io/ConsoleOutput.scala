package sgit.io

object ConsoleOutput {
  def printToScreen(toPrint: String): Unit = println(toPrint)

  def printError(errorMsg: String): Unit = System.err.println(errorMsg)

  def printGreen(toPrint: String): Unit = println("\u001B[32m" + toPrint + "\u001B[0m")

  def printRed(toPrint: String): Unit = println("\u001B[31m"+ toPrint + "\u001B[0m")

  def printYellow(toPrint: String): Unit = println("\u001B[33m" + toPrint +  "\u001B[0m")
}
