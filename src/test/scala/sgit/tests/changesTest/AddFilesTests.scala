package sgit.tests.changesTest

import org.scalatest._
import better.files._
import sgit.changes.AddFiles
import sgit.create.InitializeRepository

class AddFilesTests  extends FunSpec with BeforeAndAfter with Matchers {

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

  describe("With a new file to add"){
    it("Should add it to objects") {
      val test: File = "test.txt".toFile
      (".sgit/objects/" + test.sha1).toFile.exists should be
      (".sgit/objects/" + test.sha1).toFile.contentAsString should include ("test")

    }
    it("Should add to stage") {
      val test: File = "test.txt".toFile
      ".sgit/staged".toFile.contentAsString should  include (test.sha1 + " test.txt")
    }
  }

  describe("When adding a file already added"){
    describe("But not modified"){
      it("Should not change anything") {
        val test: File = "test.txt".toFile
        val firstFileContent = (".sgit/objects/" + test.sha1).toFile.contentAsString
        val firstStageContent = ".sgit/staged".toFile.contentAsString
        AddFiles.add(Seq(test.toJava))
        val secondStageContent = ".sgit/staged".toFile.contentAsString

        assert((".sgit/objects/" + test.sha1).toFile.exists)
        (".sgit/objects/" + test.sha1).toFile.contentAsString should include (firstFileContent)
        secondStageContent should include (firstStageContent)

      }
    }
    describe("But it has been modified") {
      it("Should replace the original file") {
        val test: File = "test.txt".toFile
        val firstsha = test.sha1
        test.appendLine("Yes, this is still a test.")
        AddFiles.add(Seq(test.toJava))
        assert(test.sha1 != firstsha)
        assert(!(".sgit/objects/"+firstsha).toFile.exists)
        assert((".sgit/objects/"+test.sha1).toFile.exists)
        ".sgit/staged".toFile.contentAsString should not include (firstsha + " test.txt")
        ".sgit/staged".toFile.contentAsString should include (test.sha1 + " test.txt")
      }
    }
  }
}
