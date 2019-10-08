package sgit.changesTest

import org.scalatest._
import sgit.changes.AddFiles
import better.files._
import sgit.create.InitializeRepository

class AddFilesTests  extends FunSpec with BeforeAndAfter  {

  before{
    if(".sgit".toFile.exists) ".sgit".toFile.delete()
    InitializeRepository.createFolder()
  }
  after {
    val _ : File = ".sgit"
      .toFile
      .delete()
    if("test.txt".toFile.exists) "test.txt".toFile.delete()
  }

  describe("With a new file to add"){
    it("Should add it to objects") {
      val test : File = "test.txt".toFile.append("test")
      AddFiles.add(Seq(test.toJava))
      println((".sgit/objects/" + test.sha1).toFile.exists)
      assert((".sgit/objects/" + test.sha1).toFile.exists)
    }
    it("Should add to stage") {
      val test : File = "test.txt".toFile.append("test")
      AddFiles.add(Seq(test.toJava))
      println(".sgit/staged".toFile.contentAsString)
      assert(".sgit/staged".toFile.contentAsString.contains((test.sha1 + " test.txt")))
    }
  }
}
