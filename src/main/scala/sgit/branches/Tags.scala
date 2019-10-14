package sgit.branches

import sgit.io.{CommitManipulation, ConsoleOutput, RefManipulation}

object Tags {

  /**
   * Prints all the tags of a repository.
   */
  def printAllTags(): Boolean = {
    val tags = RefManipulation.getAllTags
    if(tags.isEmpty) {
      ConsoleOutput.printToScreen("No tags yet.")
      false
    }
    else {
      tags.get.foreach(tag => ConsoleOutput.printToScreen(tag._1 + ": " + tag._2))
      true
    }
  }

  /**
   * Creates a tag in the repository.
   * The tag references the most recent commit.
   * If no parameter, prints all the tags.
   * @param tagName the tag name if given.
   * @return true if the operation was successful, false otherwise.
   */
  def handleTag(tagName: Option[String]): Boolean =  {
    if(tagName.isEmpty) printAllTags()
    else {
      val commit = CommitManipulation.findMostRecentCommit()
      if(commit.isEmpty) {
        ConsoleOutput.printError("There is no commit yet, exiting.")
        false
      }
      else {
        val res = RefManipulation.createTag(tagName.get, commit.get)
        if(res) ConsoleOutput.printToScreen("Successfully created tag " + tagName.get + " for commit " + commit.get)
        else ConsoleOutput.printError("Error while creating tag. Maybe the tag name is already in use?")
        res
      }
    }
  }
}
