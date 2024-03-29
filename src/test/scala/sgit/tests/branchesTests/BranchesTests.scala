package sgit.tests.branchesTests

import org.scalatest._
import better.files._
import sgit.changes.{AddFiles, CommitFiles}
import sgit.create.InitializeRepository
import sgit.refs.Branches
import sgit.io.{CommitManipulation, RefManipulation}

class BranchesTests extends FunSpec with BeforeAndAfter with Matchers {
  before{
    if("test.txt".toFile.exists) "test.txt".toFile.delete()
    if(".sgit".toFile.exists) ".sgit".toFile.delete()
    InitializeRepository.createFolder()
    val test : File = "test.txt".toFile.appendLine("test")
    AddFiles.add(Seq(test.toJava))
  }
  after {
    if(".sgit".toFile.exists) ".sgit".toFile.delete()
    if("test.txt".toFile.exists) "test.txt".toFile.delete()
  }

  describe("With no commit"){
    it("Should not branch"){
      val res = Branches.handleBranches(Some("branch"), verbose = false)
      assert(!res)
    }
  }
  describe("With a branch existing") {
    it("Should create a new branch"){
      CommitFiles.commit("")
      assert(Branches.handleBranches(Some("branch"), verbose = false))
    }
    it("Should reference the same commit as the last") {
      val res: String = CommitFiles.commit("").get
      Branches.handleBranches(Some("branch"), verbose = false)
      ".sgit/refs/heads/branch".toFile.contentAsString should include (res)
    }
    it("Should be referenced as the HEAD"){
      CommitFiles.commit("")
      Branches.handleBranches(Some("branch"), verbose = false)
      RefManipulation.getBranchName should equal ("branch")
    }
  }
}
