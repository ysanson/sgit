package sgit.io

import better.files._
import sgit.objects.{Blob, StagedFile}

object StageManipulation {
  /**
   * Gets the staged files from the sgit folder.
   * @return a sequence of files
   */
  def retrieveStagedFiles(): Option[Seq[StagedFile]] = {
    val stagedFile : File = ".sgit/staged"
      .toFile
    if(!stagedFile.exists) None
    else {
      val content = stagedFile.contentAsString().split("\n")
      if(content.isEmpty) return None
      Some(content.map((file: String) => {
        val ids = file.split(" ")
        StagedFile(ids(0), ids(1))
      }))
    }
  }

  /**
   * Add files to the staged file.
   * @param fileSignatures the sequence of file signatures (sha1)
   * @return an option, None if the folder doesn't exist, true otherwise.
   */
  def addFilesToStaged(fileSignatures: Seq[Blob]): Option[Boolean] = {
    if(!".sgit/staged".toFile.exists) None
    else {
      val stagedFile: File = ".sgit/staged".toFile
      val content: String = stagedFile.contentAsString
      fileSignatures.foreach(file => {
        if(!content.contains(file.shaPrint)) stagedFile.appendLine(file.shaPrint + " " + file.path)
      })
      Some(true)
    }
  }

  /**
   * Takes the existing lines from the staged file, removes the elements to removes, and writes back.
   * @param fileSignatures the file signatures to remove
   * @return None if error, Some(true) otherwise.
   */
  def removeFilesFromStaged(fileSignatures: Seq[String]): Option[Boolean] = {
    if(!".sgit/staged".toFile.exists) None
    else {
      val stagedFile: File = ".sgit/staged".toFile
      val content: Seq[String] = stagedFile.contentAsString.split("\n")
      val newContent = content.filterNot(key => fileSignatures.contains(key.split(" ")(0)))
      stagedFile.overwrite("")
      newContent.foreach(line => stagedFile.appendLine(line))
      Some(true)
    }
  }
}
