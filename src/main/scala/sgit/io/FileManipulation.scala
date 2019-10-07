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

    content.map((file: String) => file.toFile)
  }

  /**
   * Adds files to the storage.
   * Creates a file in the objects folder for each entry file.
   * @return the sha1 hashes for each file, or none if the directory does not exist.
   */
  def addFilesToObjects(files: Seq[File]): Option[Seq[String]] = {
    if(!".sgit/objects".toFile.exists) None
    else {
      val rootDir = ".sgit".toFile.parent
      val res = files.iterator.map(f => {
        val _ : File = (".sgit/objects/" + f.sha1)
          .toFile
          .appendLine(rootDir.relativize(f).toString)
          .appendText(f.contentAsString)
        f.sha1
      }).toSeq
      Some(res)
    }
  }

  /**
   * Add files to the staged file.
   * @param fileSignatures the sequence of file signatures (sha1)
   * @return an option, None if the fodler doesn't exist, true otherwise.
   */
  def addFilesToStaged(fileSignatures: Seq[String]): Option[Boolean] = {
    if(!".sgit/staged".toFile.exists) None
    else {
      val stagedFile: File = ".sgit/staged".toFile
      fileSignatures.foreach(signature => stagedFile.appendLine(signature))
      Some(true)
    }
  }

  def getFile(fileName: String): Seq[File] = {
    if(!fileName.contains('*') || fileName.toFile.exists) Seq(fileName.toFile)
    else {
      val rootDir = ".sgit".toFile.parent
      rootDir.glob(fileName).toSeq
    }
  }
}
