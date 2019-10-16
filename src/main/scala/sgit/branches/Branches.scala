package sgit.branches

import sgit.io.{CommitManipulation, ConsoleOutput, RefManipulation}

object Branches {

  /**
   * Lists all the existing branches.
   * @return true if everything went correctly, false otherwise.
   */
  def listBranches(verbose: Boolean): Boolean = {
    val branches = RefManipulation.getAllBranches
    if(branches.isEmpty){
      ConsoleOutput.printError("No commits yet. Try committing something with sgit commit.")
      false
    } else {
      val currentBranch: String = RefManipulation.getBranchName

      branches.get.foreach(branch => {
        if(verbose) {
          val commit = CommitManipulation.findCommitInfos(branch._2)
          if(branch._1 == currentBranch)
            ConsoleOutput.printGreen("*Branch " + branch._1 + " -> " + branch._2  + ": " + commit.get.desc)
          else
            ConsoleOutput.printToScreen("Branch " + branch._1 + " -> " + branch._2  + ": " + commit.get.desc)
        } else if(branch._1 == currentBranch)
          ConsoleOutput.printGreen("*Branch " + branch._1)
        else
          ConsoleOutput.printToScreen("Branch " + branch._1)
      })
      true
    }
  }

  /**
   * Handles the command branch. If a parameter name is given, creates a new branch. If not, prints all the branches.
   * @param branchName the optional branch name.
   * @return Boolean if everything went up correctly or not.
   */
  def handleBranches(branchName: Option[String], verbose: Boolean): Boolean = {
    if(branchName.isEmpty) listBranches(verbose)
    else {
      if(RefManipulation.getBranchName == branchName.get)
        ConsoleOutput.printGreen("Already on branch " + branchName.get)
      val commit = CommitManipulation.findMostRecentCommit()
      if(commit.isEmpty){
        ConsoleOutput.printError("No commits yet. Commit first before creating a branch.")
        false
      } else {
        val isCreated = RefManipulation.createBranch(branchName.get, commit.get)
        if(isCreated) {
          RefManipulation.updateHead(branchName.get)
          ConsoleOutput.printGreen("Created branch " + branchName.get + " at commit " + commit.get)
          true
        } else {
          ConsoleOutput.printError("Cannot create branch right now.")
          false
        }
      }
    }
  }
}
