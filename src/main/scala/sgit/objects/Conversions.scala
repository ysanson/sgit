package sgit.objects

import scala.annotation.tailrec

object Conversions {
  def convertBlobsToStagedFiles(blobs: Seq[Blob]): Seq[StagedFile] = {
    @tailrec
    def convert(blobs: Seq[Blob], res: Seq[StagedFile]): Seq[StagedFile] = {
      if(blobs.isEmpty) return res
      convert(blobs.tail, StagedFile(blobs.head.shaPrint, blobs.head.path)+:res)
    }
    convert(blobs, Seq())
  }
}
