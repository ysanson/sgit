package sgit.objects

import better.files.File
import sgit.io.FileManipulation

import scala.annotation.tailrec

object Conversions {
  /**
   * Converts a blob file (in memory) to a staged file (in memory).
   * These files aren't actually written, it's their representation in memory.
   * It uses a tail recursion for improved performance.
   * @param blobs the sequence of blobs to convert.
   * @return the sequence of staged files representing the blobs.
   */
  def convertBlobsToStagedFiles(blobs: Seq[Blob]): Seq[StagedFile] = {
    @tailrec
    def convert(blobs: Seq[Blob], res: Seq[StagedFile]): Seq[StagedFile] = {
      if(blobs.isEmpty) return res
      convert(blobs.tail, StagedFile(blobs.head.shaPrint, blobs.head.path)+:res)
    }
    convert(blobs, Seq())
  }

  /**
   * Converts a file to a staged file representation.
   * @param files the sequence of files to convert
   * @return the sequence of staged file returned.
   */
  def convertFileToStagedFile(files: Seq[File]): Seq[StagedFile] = {
    @tailrec
    def convert(files: Seq[File], res: Seq[StagedFile]): Seq[StagedFile] = {
      if (files.isEmpty) return res
      convert(files.tail, res :+ StagedFile(files.head.sha1, FileManipulation.relativizeFilePath(files.head).orNull))
    }
    convert(files, Seq())
  }

  /**
   * Converts staged files to blobs.
   * @param staged the files to convert
   * @return the blobs
   */
  def convertStagedFilesToBlobs(staged: Seq[StagedFile]): Seq[Blob] = {
    @tailrec
    def convert(files: Seq[StagedFile], res: Seq[Blob]): Seq[Blob] = {
      if(files.isEmpty) res
      else {
        val stagedHead: StagedFile = staged.head
        val file: File = FileManipulation.retrieveFileFromObjects(stagedHead.shaPrint).get
        val blob= Blob(file.contentAsString.substring(file.contentAsString.indexOf("\n")), stagedHead.name, stagedHead.shaPrint)
        convert(files.tail, res:+blob)
      }
    }
    convert(staged, Seq())
  }

  /**
   * gets the names and the sha prints from a sequence of staged files.
   * @param stagedFiles the staged files.
   * @return a tuple, the first one containing the name and the second the sha prints.
   */
  def nameAndShaFromStageFiles(stagedFiles: Seq[StagedFile]): (Seq[String], Seq[String]) = (stagedFiles.map(f => f.name), stagedFiles.map(f => f.shaPrint))
}
