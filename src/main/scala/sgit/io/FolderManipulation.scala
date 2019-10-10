package sgit.io

import java.nio.file.{Files, Paths}

import better.files._
import sgit.objects.{Blob, Folder, TreeObject}

/**
 * Contains all the methods to manipulate folders in the project.
 */
object FolderManipulation {
  /**
   * Checks if the folder is present.
   * @param folderName the folder name
   * @return true if the folder exists, false otherwise.
   */
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
        .createIfNotExists(isDir, createParents = true)
    }
    createFile(".sgit/objects", true)
    createFile(".sgit/refs/heads", true)
    createFile(".sgit/refs/tags", true)
    createFile(".sgit/staged", false)
    val _ : File = ".sgit/HEAD"
      .toFile
      .createIfNotExists(asDirectory = false, createParents = true)
      .append("refs/heads/master")
    true
  }

  /**
   * Lists all the files that have been modified compared to their version in sgit.
   * @param folder the folder to search
   * @return a sequence of modified files, or None if there is none
   */
  def findDifferentFilesForFolder(folder: File, storedFolder: Folder): Option[Seq[File]] =  {
    if(!folder.isDirectory) None
    else {
      //Gets the files in the given directory
      val files: Iterator[File] = folder.children.filter(file => !file.isDirectory)
      //Gets the names of the files, relative to the root folder.
      val fileNames: List[String] = files.map(file => FileManipulation.relativizeFilePath(file).get).toList
      //Gets the stored files content's.
      val storedFiles = storedFolder.children.map((ch: TreeObject) => {
        ch match {
          case blob: Blob => blob.shaPrint
          case _ => null
        }
      })
      //Gets the stored files' names.
      val storedFileNames = storedFolder.children.map((ch: TreeObject) => {
        ch match {
          case blob: Blob => blob.path
          case _ => null
        }
      })

      //Files that content is different from the stored version.
      val modifiedFiles: Seq[File] = files.filterNot(file => storedFiles.contains(file.sha1)).toSeq
      //Files that name doesn't appear in the stored file names.
      val newFiles: Seq[File] = files.filterNot(file => storedFileNames.contains(FileManipulation.relativizeFilePath(file).get)).toSeq
      //Files that name appears in the stored file names but not in the working directory.
      val deletedFiles: Seq[File] = storedFileNames
        .filterNot(name => fileNames.contains(name))
        .map(deleted => deleted.toFile)
      Some(modifiedFiles.concat(newFiles).concat(deletedFiles))
    }
  }

  /**
   * Lists all the subdirectories of a given folder.
   * Also lists the folder
   * @param baseDir the base directory
   * @return an option containing a list of files.
   */
  def listSubDirectories(baseDir: File): Option[List[File]] = {
    if(!baseDir.isDirectory && ".sgit".toFile.exists) None
    else {
      val dirList: List[File] = baseDir::baseDir
        .listRecursively()
        .filter(file => file.isDirectory)
        .toList
      Some(dirList)
    }
  }

  def listAllChildren(baseDir: File): Option[List[File]] = {
    if(!baseDir.isDirectory && ".sgit".toFile.exists) None
    else Some(baseDir::baseDir.listRecursively.toList)
  }
}
