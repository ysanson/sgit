package sgit.io

import better.files._
import sgit.objects.Blob

object FileManipulation {

  /**
   * Retrieves the file from the storage.
   * @param signature the file signature
   * @return the file.
   */
  def retrieveFileFromObjects(signature: String): Option[File] = {
    if((".sgit/objects/"+signature).toFile.exists) Some((".sgit/objects/"+signature).toFile)
    else None
  }

  /**
   * Removes a file from the storage.
   * @param signature the signature of the file
   * @return None if the file doesn't exist, true if it has been deleted.
   */
  def removeFileFromObjects(signature: String): Option[Boolean] = {
    val file = retrieveFileFromObjects(signature)
    if(file.isEmpty) return None
    file.get.delete()
    Some(true)
  }

  /**
   * Writes a file in the objects folder.
   * @param file the input file
   */
  def writeFileInObject(file: File): Unit = (".sgit/objects/"+file.sha1)
    .toFile
    .overwrite(relativizeFilePath(file).get)
    .appendLine()
    .appendLine(file.contentAsString)
  /**
   * Gets the file path from the root of the working directory.
   * @param file the file to relativize the path from
   * @return an option containing the path, or none if the path doesn't exist.
   */
  def relativizeFilePath(file: File): Option[String] = {
    if(!".sgit".toFile.exists || !file.exists) return None
    val rootDir = ".sgit".toFile.parent
    Some(rootDir.relativize(file).toString)
  }
  /**
   * Adds files to the storage.
   * Creates a file in the objects folder for each entry file.
   * @return the sha1 hashes for each file, or none if the directory does not exist.
   */
  def addFilesToObjects(files: Seq[File]): Option[Seq[Blob]] = {
    if(!".sgit/objects".toFile.exists) None
    else {
      val res = files.iterator.map(f => {
        writeFileInObject(f)
        Blob(f.contentAsString, relativizeFilePath(f).get, f.sha1)
      }).toSeq
      Some(res)
    }
  }

  /**
   * Gets the files related to the name or regular expression.
   * @param fileName the file name or the regular expression.
   * @return a sequence of files.
   */
  def getFile(fileName: String): Seq[File] = {
    if(!fileName.contains('*') && (fileName.toFile.exists && !fileName.toFile.isDirectory)) Seq(fileName.toFile)
    else {
      val rootDir = ".sgit".toFile.parent
      rootDir.glob(fileName).toSeq
    }
  }

  /**
   * Compares two files and checks if their contents are the same.
   * @param file1 the first file
   * @param file2 the second file
   * @return an option, none if one of the file is not present, true if the contents are the same, false otherwise.
   */
  def compareFiles(file1: File, file2: File): Option[Boolean] = {
    if (!file1.exists || !file2.exists) None
    else Some(file1.isSameContentAs(file2))
  }
}
