package sgit.io

import better.files._

object FileManipulation {
  /**
   * Gets the staged files from the sgit folder.
   * @return a sequence of files
   */
  def retrieveStagedFiles(): Seq[File] = {
    val stagedFile : File = ".sgit/staged"
      .toFile
    val content = stagedFile.contentAsString().split("\n")
    content.map((file: String) => (".sgit/objects/"+file).toFile)
  }

  /**
   * Gets the staged file names.
   * @return a sequence of string indicating the file names.
   */
  def retrieveStagedFileNames(): Seq[String] = retrieveStagedFiles().map(f => f.contentAsString.substring(0, f.contentAsString.indexOf('\n')))

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
  def addFilesToObjects(files: Seq[File]): Option[Seq[String]] = {
    if(!".sgit/objects".toFile.exists) None
    else {
      val res = files.iterator.map(f => {
        val _ : File = (".sgit/objects/" + f.sha1)
          .toFile
          .overwrite(relativizeFilePath(f).getOrElse("UNKNOWN"))
          .appendLine()
          .appendLine(f.contentAsString)
        f.sha1
      }).toSeq
      Some(res)
    }
  }

  /**
   * Add files to the staged file.
   * @param fileSignatures the sequence of file signatures (sha1)
   * @return an option, None if the folder doesn't exist, true otherwise.
   */
  def addFilesToStaged(fileSignatures: Seq[String]): Option[Boolean] = {
    if(!".sgit/staged".toFile.exists) None
    else {
      val stagedFile: File = ".sgit/staged".toFile
      val content: String = stagedFile.contentAsString
      fileSignatures.foreach(signature => {
        if(!content.contains(signature)) stagedFile.appendLine(signature)
      })
      Some(true)
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
