package sgit.changes

import sgit.io.{CommitManipulation, ConsoleOutput, RefManipulation, StageManipulation}
import sgit.objects.{Commit, StagedFile}

object CommitFiles {

  def executeCommit(files: Seq[StagedFile], description: String, parents: (String,String)): String = {
    val commitSha = CommitManipulation.createCommit(files, description, parents)
    val branchName = RefManipulation.updateCurrBranch(commitSha)
    StageManipulation.emptyStage()
    ConsoleOutput.printToScreen("New commit " + commitSha + " at branch " + branchName.get)
    commitSha
  }

  /**
   * Creates a new commit in the repository.
   */
  def commit(): Option[String] = {
    val lastCommit: Option[String] = CommitManipulation.findMostRecentCommit()
    val stagedFiles: Option[Seq[StagedFile]] = StageManipulation.retrieveStagedFiles()

    if(stagedFiles.isEmpty) {
      ConsoleOutput.printError("No files have been staged. Please run sgit add <files>")
      None
    }
    else if(lastCommit.isEmpty) {
      Some(executeCommit(stagedFiles.get, "Initial commit", ("", "")))
    } else {

      val last: Commit = CommitManipulation.findCommitInfos(lastCommit.get).get
      val newFilesNames: Seq[String] = stagedFiles.get.map(file => file.name)
      val newList: Seq[StagedFile] = last.files.iterator.map(file => {
        if(newFilesNames.contains(file.name)) stagedFiles.get.filter(f => f.name == file.name).head
        else file
      }).toIndexedSeq
      Some(executeCommit(newList.concat(stagedFiles.get).distinct, "A description", (last.name, "")))
    }
  }
}
