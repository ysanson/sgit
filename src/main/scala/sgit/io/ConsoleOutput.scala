package sgit.io

object ConsoleOutput {
  def printToScreen(toPrint: String): Unit = println(toPrint)

  def printError(errorMsg: String, error: Error): Unit = System.err.println(errorMsg + "\n" + error.getMessage)
}
