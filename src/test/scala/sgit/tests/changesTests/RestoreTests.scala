package sgit.tests.changesTests

import org.scalatest.{BeforeAndAfter, FunSpec, Matchers}
import better.files._
import sgit.refs.Branches
import sgit.changes.{AddFiles, CommitFiles, Restore}
import sgit.io.{CommitManipulation, FolderManipulation, RefManipulation}

class RestoreTests extends FunSpec with BeforeAndAfter with Matchers  {
  before{
    if(".sgit".toFile.exists) ".sgit".toFile.delete()
    FolderManipulation.createFolderStructure()
    "test.txt".toFile.createIfNotExists().append("Test")
    AddFiles.add(Seq("test.txt".toFile.toJava))
  }

  after {
    if(".sgit".toFile.exists) ".sgit".toFile.delete()
    if("test.txt".toFile.exists) "test.txt".toFile.delete()
  }

  describe("With no commit") {
    it("Should not checkout") {
      val res = Restore.checkout("")
      assert(!res)
    }
  }
  describe("With a commit"){
    describe("And the wrong commit given"){
      it("Should return false"){
        CommitFiles.commit("A commit")
        val res = Restore.checkout("")
        assert(!res)
      }
    }
    describe("And the commit given in parameter"){
      it("Should return true"){
        val commitName = CommitFiles.commit("").get
        val res = Restore.checkout(commitName)
        assert(res)
      }
    }
  }
  describe("With a new branch"){
    it("Should update the head"){
      CommitFiles.commit("")
      Branches.handleBranches(Some("newBranch"), verbose = false)
      val res = Restore.checkout("newBranch")
      assert(res)
      RefManipulation.getBranchName should equal ("newBranch")
    }
    it("Should discard the modification of the commit") {
      val firstCommit = CommitFiles.commit("").get
      "test.txt".toFile.appendLine("Hello there")
      Branches.handleBranches(Some("newBranch"), verbose = false)
      AddFiles.add(Seq("test.txt".toFile.toJava))
      val secondCommit = CommitFiles.commit("Second commit").get
      Restore.checkout("master")
      "test.txt".toFile.contentAsString should not contain "Hello there"
    }
  }
}
