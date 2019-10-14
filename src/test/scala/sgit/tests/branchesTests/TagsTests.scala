package sgit.tests.branchesTests

import org.scalatest._
import better.files._
import net.bytebuddy.dynamic.scaffold.TypeInitializer.None
import sgit.changes.{AddFiles, CommitFiles}
import sgit.create.InitializeRepository
import sgit.branches.Tags
import sgit.io.{CommitManipulation, RefManipulation}

class TagsTests extends FunSpec with BeforeAndAfter with Matchers {
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
    it("Should return false"){
      val created = Tags.handleTag(Some("tag"))
      assert(!created)
    }
    it("Should list no commit") {
      assert(RefManipulation.getAllTags.isEmpty)
    }
  }
  describe("With a commit") {
    it("Should create a tag") {
      CommitFiles.commit("")
      val created = Tags.handleTag(Some("A tag"))
      assert(created)
      ".sgit/refs/tags".toFile.children.toSeq should have length 1
    }
    it("Should reference the last tag") {
      CommitFiles.commit("")
      Tags.handleTag(Some("tag"))
      ".sgit/refs/tags/tag".toFile.contentAsString should include (CommitManipulation.findMostRecentCommit().get)
    }
    it("Should list 1 tag") {
      CommitFiles.commit("")
      Tags.handleTag(Some("tag"))
      assert(RefManipulation.getAllTags.get.size == 1)
    }
  }
}
