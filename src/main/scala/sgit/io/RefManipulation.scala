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

  /**
   * Gets the current branch name.
   * @return the branch name.
   */
  def getBranchName: String = {
    if(!".sgit/HEAD".toFile.exists) ""
    else {
      ".sgit/HEAD".toFile
        .contentAsString
        .split("/")
        .last
    }
  }

  /**
   * Creates a new tag.
   * @param tagName the tag name
   * @param commitName the commit to affect the tag to.
   * @return True if the commit has been created, false otherwise.
   */
  def createTag(tagName: String, commitName: String): Boolean = {
    if(".sgit/refs/tags".toFile.exists) {
      if((".sgit/refs/tags" + tagName).toFile.exists) false
      else {
        (".sgit/refs/tags/"+tagName).toFile.append(commitName)
        true
      }
    }else false
  }

  /**
   * Gets all the tags from the repository.
   * @return an optional map of tags, name -> commit
   */
  def getAllTags: Option[Map[String, String]] = {
    if(".sgit/refs/tags".toFile.isEmpty) None
    else {
      val tags = ".sgit/refs/tags".toFile.children.toIndexedSeq.sorted(File.Order.byModificationTime)
      Some(tags.map(tag => (tag.name, tag.contentAsString.replace("\r", ""))).toMap)
    }
  }
}
