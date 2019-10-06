package sgit.io

import better.files._
import sgit.objects.Commit

object CommitManipulation {
  /**
   * Finds the most recent commit.
   * @return the commit id.
   */
  def findMostRecentCommit(): String = {
    val ref : String = ".sgit/HEAD"
      .toFile
      .contentAsString()
      .substring(4)
    ref.toFile.contentAsString
  }

  def findCommitInfos(commit: String): Commit = {
    val values: Array[String] = (".sgit/objects/" + commit).toFile.contentAsString.split("\n")
    Commit(commit,
      values(1).split(" ")(1),
      values(2).split(" ")(1),
      values(3).split(" ")(1))
  }
}
