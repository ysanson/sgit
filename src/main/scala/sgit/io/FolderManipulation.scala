package sgit.io

import java.nio.file.{Files, Paths}

import better.files._

/**
 * Contains all the methods to manipulate folders in the project.
 */
object FolderManipulation {
  def isFolderPresent(folderName: String): Boolean = Files.exists(Paths.get(folderName))

  def createFolderStructure(): Boolean = {
    if(isFolderPresent(".sgit")) return false
    val _ : File = ".sgit"
      .toFile
      .createDirectory()
    val _ : File = ".sgit/objects"
      .toFile
      .createDirectory()
    val _ : File = ".sgit/refs/heads"
      .toFile
      .createDirectoryIfNotExists(true)
    val _ : File = ".sgit/refs/tags"
      .toFile
      .createDirectoryIfNotExists(true)
    val _ : File = ".sgit/HEAD"
      .toFile
      .createIfNotExists(false, true)
      .append("ref: refs/heads/master")
    val _ : File = ".sgit/staged"
      .toFile
      .createIfNotExists(false, true)
    true
  }
}
