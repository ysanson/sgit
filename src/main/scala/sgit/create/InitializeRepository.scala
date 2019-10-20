package sgit.create

import sgit.io.{ConsoleOutput, FolderManipulation}

object InitializeRepository {
  /**
   * Creates a new sgit folder in the repository.
   */
  def createFolder(): Unit = {
    ConsoleOutput.printToScreen("Initializing repository.")
    val result = FolderManipulation.createFolderStructure()
    if(result) ConsoleOutput.printToScreen("Repository initialized!")
    else ConsoleOutput.printError("Error: .sgit folder already exists!")
  }
}
