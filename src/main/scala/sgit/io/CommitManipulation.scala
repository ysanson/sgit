package sgit.io

import better.files._
import sgit.objects.{Commit, StagedFile}

object CommitManipulation {
  /**
   * Finds the most recent commit.
   * @return the commit id.
   */
  def findMostRecentCommit(): Option[String] = {
    val ref : String = ".sgit/HEAD"
      .toFile
      .contentAsString()
    if((".sgit/"+ref).toFile.exists)
      Some((".sgit/"+ref).toFile.contentAsString)
    else None
  }

  /**
   * Finds the commit infos by its ID.
   * Commit structure:
   * desc: "desc"
   * parent: "SHA prints separated by spaces"
   * The following lines are the files.
   * @param commit the commit id
   * @return a, option with a commit object containing the infos.
   */
  def findCommitInfos(commit: String): Option[Commit] = {
    if(!(".sgit/objects/"+commit).toFile.exists) return None

    val values: Array[String] = (".sgit/objects/" + commit).toFile
      .contentAsString.replace("\r", "")
      .split("\n")

    val files: List[StagedFile] = values.drop(2).map(line => {
      val content = line.split(" ")
      StagedFile(content(0), content(1))
    }).toList

    Some(Commit(
      commit,
      values(0),
      values(1).split(" ").toIndexedSeq,
      files
    ))
  }

  /**
   * Creates a commit file on the objects directory.
   * @param filesToCommit the files to commit.
   * @param description the commit description
   * @param parents the parents of the commit.
   */
  def createCommit(filesToCommit: Seq[StagedFile], description: String, parents: (String, String)): String = {
    val comm: File = ".sgit/objects/tempcommit".toFile
      .createIfNotExists()
      .appendLine(description)
      .appendLine(parents._1 + " " + parents._2)
    filesToCommit.foreach(line => comm.appendLine(line.shaPrint + " " + line.name))
    comm.renameTo(comm.sha1).sha1
  }

}
