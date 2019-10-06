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

    content.map((file: String) => {
      val stored: File = (".sgit/objects/"+file)
          .toFile
      stored
    })
  }

  /**
   * Adds files to the staged stage.
   * Creates a file in the objects folder for each entry file.
   * @return the sha1 hashes for each file.
   */
  def addFilesToStage(files: Seq[File]): Seq[String] = {
    files.iterator.map(f => {
      val _: File = (".sgit/objects/" + f.sha1)
        .toFile
        .appendLine(f.pathAsString)
        .appendText(f.contentAsString)
      f.sha1
    }).toSeq
  }

  def findFiles(fileName: String): Seq[File] = {
    println(fileName)
    if(!fileName.contains('*') && fileName.toFile.exists) return Seq(fileName.toFile)
    val dir = ".sgit".toFile.parent
    dir.glob(fileName).toSeq
  }

  def compareFiles(file1: File, file2: File): Option[Boolean] = {
    if (!file1.exists || !file2.exists) None
    else Some(file1.isSameContentAs(file2))
  }
}
