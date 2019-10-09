package sgitTests.io

object ConsoleOutput {
  def printToScreen(toPrint: String): Unit = println(toPrint)

  def printError(errorMsg: String): Unit = System.err.println(errorMsg)
}
