package sgit.tests.changesTest

import org.scalatest.{BeforeAndAfter, FunSpec, Matchers}
import better.files._
import sgit.changes.{AddFiles, CommitFiles, WorkspaceStatus}
import sgit.io.{CommitManipulation, FileManipulation, FolderManipulation, StageManipulation}
import sgit.objects.StagedFile

class StatusTest extends FunSpec with BeforeAndAfter with Matchers  {
  before{
    if(".sgit".toFile.exists) ".sgit".toFile.delete()
    FolderManipulation.createFolderStructure()
    "test.txt".toFile.createIfNotExists().append("Test")
  }

  after {
    if(".sgit".toFile.exists) ".sgit".toFile.delete()
    if("test.txt".toFile.exists) "test.txt".toFile.delete()
  }

  describe("With no commits or stage"){
    it("Should list all files as untracked"){
      val allFiles = ".sgit".toFile.parent.listRecursively.filterNot(file => file.isDirectory).toSeq
      val res = FileManipulation.searchUntrackedFiles(allFiles)
      res.length should equal (allFiles.length)
    }
  }
  describe("With a file in the stage") {
    it("Should have one file in ready to be committed"){
      val test = "test.txt".toFile.appendLine("test")
      AddFiles.add(Seq(test.toJava))
      val stageDiff: Option[Seq[StagedFile]] = StageManipulation.retrieveStagedFiles()
      val res = WorkspaceStatus.readyToBeCommitted(stageDiff, None)
      res.get should have length 1
    }
  }
  describe("Wih a file commited and modified") {
    it("Should list it as not staged for commit") {
      val test = "test.txt".toFile.appendLine("test")
      AddFiles.add(Seq(test.toJava))
      CommitFiles.commit("")
      test.appendLine("Hello there")
      val allFiles: List[File] = FolderManipulation.listAllChildren(".sgit".toFile.parent).get.filterNot(file => file.isDirectory)
      val notInObjects: Seq[File] = FileManipulation.searchUntrackedFiles(allFiles)
      val commitedFiles = CommitManipulation.findCommitInfos(CommitManipulation.findMostRecentCommit().get).get.files
      val res = WorkspaceStatus.notStagedForCommit(notInObjects,Some(commitedFiles), None)
      res._1.get should have length 1
    }
  }
}
