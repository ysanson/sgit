package sgit.create

import sgit.io.FolderManipulation
import sgit.io.ConsoleOutput

object InitializeRepository {
  def createFolder(): Unit = {
    ConsoleOutput.printToScreen("Initializing repository.")
    val result = FolderManipulation.createFolderStructure()
    if(result) ConsoleOutput.printToScreen("Repository initialized!")
    else ConsoleOutput.printToScreen("Error: .sgit folder already exists!")
  }
}
