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
   * Transforms the file in the objects to a blob.
   * @param signature the file's signature.
   * @return an optional blob.
   */
  def findBlob(signature: String): Option[Blob] = {
    val file = retrieveFileFromObjects(signature)
    if(file.isEmpty) None
    else {
      val content = file.get.contentAsString.replace("\r", "")
      Some(Blob(content.substring(content.indexOf("\n")+1), content.substring(0, content.indexOf("\n")), file.get.sha1))
    }
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
  def writeFileInObject(file: File): Unit =
    (".sgit/objects/"+file.sha1)
      .toFile
      .overwrite(relativizeFilePath(file).get)
      .appendLine()
      .append(file.contentAsString)
  /**
   * Gets the file path from the root of the working directory.
   * @param file the file to relativize the path from
   * @return an option containing the path, or none if the path doesn't exist.
   */
  def relativizeFilePath(file: File): Option[String] = {
    if(!".sgit".toFile.exists || !file.exists) return None
    val rootDir = ".".toFile
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
  def findFile(fileName: String): Seq[File] = {
    if(!fileName.contains('*') && (fileName.toFile.exists && !fileName.toFile.isDirectory)) Seq(fileName.toFile)
    else {
      val rootDir = ".".toFile
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

  /**
   * Searches for the untracked files in the directory.
   * @param files the files to check
   * @return a list of untracked files.
   */
  def searchUntrackedFiles(files: Seq[File]): Seq[File] = files.filterNot(file => (".sgit/objects/" + file.sha1).toFile.exists)

  /**
   * Writes a blob in the working directory, at the path specified in the bloc object.
   * @param blob the blob to write.
   */
  def writeBlobInWorkingDir(blob: Blob): Unit =
    blob.path.toFile
      .createIfNotExists(asDirectory = false, createParents = true)
      .overwrite(blob.content)

  def findAllFiles(baseDir: File): Option[List[File]] = {
    if(!baseDir.isDirectory && ".sgit".toFile.exists) None
    else Some(baseDir.listRecursively.filterNot(file => file.pathAsString.contains(".sgit") || file.isDirectory).toList)
  }
}
