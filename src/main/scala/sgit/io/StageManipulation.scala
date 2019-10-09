package sgit.io

import better.files._
import sgit.objects.{Blob, StagedFile}

import scala.annotation.tailrec

object StageManipulation {
  /**
   * Gets the staged files from the sgit folder.
   * @return a sequence of files
   */
  def retrieveStagedFiles(): Option[Seq[StagedFile]] = {
    val stagedFile : File = ".sgit/staged".toFile
    if(!stagedFile.exists) None
    else {
      val content = stagedFile.contentAsString()
        .replace("\r", "")
        .split("\n")
        .filterNot(string => string.isEmpty)
      if(content.isEmpty) return None
      Some(content.map((file: String) => {
        val ids = file.split(" ")
        StagedFile(ids(0), ids(1).replace("\r", ""))
      }))
    }
  }

  /**
   * Add files to the staged file: removes the old lines and adds others..
   * @param fileSignatures the sequence of file signatures (sha1)
   * @return an option, None if the folder doesn't exist, true otherwise.
   */
  def addFilesToStaged(fileSignatures: Seq[StagedFile]): Option[Boolean] = {
    if(!".sgit/staged".toFile.exists) None
    else {
      val stagedFile: File = ".sgit/staged".toFile.overwrite("")
      val content: String = stagedFile.contentAsString
      fileSignatures.foreach(file => {
        if(!content.contains(file.shaPrint)) stagedFile.appendLine(file.shaPrint + " " + file.name)
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

  def convertToStagedFile(files: Seq[File]): Seq[StagedFile] = {
    @tailrec
    def convert(files: Seq[File], res: Seq[StagedFile]): Seq[StagedFile] = {
      if (files.isEmpty) return res
      convert(files.tail, res :+ StagedFile(files.head.sha1, FileManipulation.relativizeFilePath(files.head).orNull))
    }
    convert(files, Seq())
  }

}
