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
   * Adds files to the staged stage.
   * Creates a file in the objects folder for each entry file.
   * @return the sha1 hashes for each file.
   */
  def addFilesToStage(files: Seq[File]): Seq[String] = {
    files.iterator.map(f => {
      val _ : File = f.sha1
        .toFile
        .appendLine(f.name)
        .appendText(f.contentAsString)
      f.sha1
    }).toSeq
  }
}
