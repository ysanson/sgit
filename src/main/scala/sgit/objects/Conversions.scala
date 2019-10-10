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
}
