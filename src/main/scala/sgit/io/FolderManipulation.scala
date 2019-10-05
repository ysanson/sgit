package sgit.io

import java.nio.file.{Files, Paths}

import better.files._

/**
 * Contains all the methods to manipulate folders in the project.
 */
object FolderManipulation {
  def isFolderPresent(folderName: String): Boolean = Files.exists(Paths.get(folderName))

  /**
   * Creates the .sgit folder structure.
   * @return true if the folder has been created correctly, false otherwise.
   */
  def createFolderStructure(): Boolean = {
    if(isFolderPresent(".sgit")) return false

    val createFile = (name: String, isDir: Boolean) => {
      val _ : File = name
        .toFile
        .createIfNotExists(isDir, true)
    }
    createFile(".sgit/objects", true)
    createFile(".sgit/refs/heads", true)
    createFile(".sgit/refs/tags", true)
    createFile(".sgit/staged", false)
    val _ : File = ".sgit/HEAD"
      .toFile
      .createIfNotExists(false, true)
      .append("ref: refs/heads/master")
    true
  }
}
