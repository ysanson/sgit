package sgitTests.create

import sgitTests.io.{ConsoleOutput, FolderManipulation}

object InitializeRepository {
  def createFolder(): Unit = {
    ConsoleOutput.printToScreen("Initializing repository.")
    val result = FolderManipulation.createFolderStructure()
    if(result) ConsoleOutput.printToScreen("Repository initialized!")
    else ConsoleOutput.printError("Error: .sgit folder already exists!")
  }
}
