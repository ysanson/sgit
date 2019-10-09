package sgitTests.io

import better.files._
import sgitTests.objects.Commit

object CommitManipulation {
  /**
   * Finds the most recent commit.
   * @return the commit id.
   */
  def findMostRecentCommit(): Option[String] = {
    val ref : String = ".sgit/HEAD"
      .toFile
      .contentAsString()
      .substring(4)
    if(ref.toFile.exists) Some(ref.toFile.contentAsString)
    else None
  }

  /**
   * Finds the commit infos by its ID.
   * @param commit the commit id
   * @return a commit object with the infos.
   */
  def findCommitInfos(commit: String): Commit = {
    val values: Array[String] = (".sgit/objects/" + commit).toFile.contentAsString.split("\n")
    Commit(commit,
      values(1).split(" ")(1),
      values(2).split(" ")(1),
      values(3).split(" ")(1))
  }
}
