package sgit.io

import better.files._

object RefManipulation {

  /**
   * Updates the current branch, and returns the name of the current branch.
   * @param newSha the new sha to refer to.
   * @return an option, none if error, or the branch name.
   */
  def updateCurrBranch(newSha: String): Option[String] = {
    if(!".sgit/HEAD".toFile.exists) return None
    val ref: String = ".sgit/HEAD".toFile.contentAsString.replace("\r", "")
      Some((".sgit/" + ref)
        .toFile
        .createIfNotExists()
        .overwrite(newSha)
        .name)
  }

  def getBranchName: String = {
    if(!".sgit/HEAD".toFile.exists) ""
    else {
      ".sgit/HEAD".toFile
        .contentAsString
        .split("/")
        .last
    }
  }
}
