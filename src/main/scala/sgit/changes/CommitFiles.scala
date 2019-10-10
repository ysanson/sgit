package sgit.changes

import sgit.io.{CommitManipulation, ConsoleOutput, RefManipulation, StageManipulation}
import sgit.objects.{Commit, StagedFile}

object CommitFiles {

  def executeCommit(files: Seq[StagedFile], description: String, parents: (String,String)): Unit = {
    val commitSha = CommitManipulation.createCommit(files, description, parents)
    val branchName = RefManipulation.updateCurrBranch(commitSha)
    StageManipulation.emptyStage()
    ConsoleOutput.printToScreen("New commit " + commitSha + " at branch " + branchName.get)
  }

  /**
   * Creates a new commit in the repository.
   */
  def commit(): Boolean = {
    val lastCommit: Option[String] = CommitManipulation.findMostRecentCommit()
    val stagedFiles: Option[Seq[StagedFile]] = StageManipulation.retrieveStagedFiles()

    if(stagedFiles.isEmpty) {
      ConsoleOutput.printError("No files have been staged. Please run sgit add <files>")
      false
    }
    else if(lastCommit.isEmpty) {
      executeCommit(stagedFiles.get, "Initial commit", ("", ""))
      true
    } else {

      val last: Commit = CommitManipulation.findCommitInfos(lastCommit.get).get
      val newFilesNames: Seq[String] = stagedFiles.get.map(file => file.name)
      val newList: Seq[StagedFile] = last.files.iterator.map(file => {
        if(newFilesNames.contains(file.name)) stagedFiles.get.filter(f => f.name == file.name).head
        else file
      }).toIndexedSeq
      executeCommit(newList.concat(stagedFiles.get).distinct, "A description", (last.name, ""))
      true
    }
  }
}
