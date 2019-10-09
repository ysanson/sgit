package sgit.tests.ioTests

import org.scalatest._
import java.nio.file.{Files, Paths}

import better.files._
import sgit.io.FolderManipulation

class FolderManipulationTests extends FunSpec with BeforeAndAfter with Matchers {

  after {
    ".sgit".toFile.delete()
  }

  describe("With no folder"){
    it("Should create the folder structure"){
      if(Files.exists(Paths.get(".sgit"))) {
        val _ : File = ".sgit"
          .toFile
          .delete()
      }
      val res = FolderManipulation.createFolderStructure()
      assert(res)
      assert(".sgit/refs/heads".toFile.exists)
      assert(".sgit/refs/tags".toFile.exists)
      assert(".sgit/objects".toFile.exists)
      assert(".sgit/staged".toFile.exists)
      ".sgit/HEAD".toFile.contentAsString should include ("ref: refs/heads/master")
    }
  }

  describe("With a folder already in place"){
    it("Should do nothing"){
      ".sgit"
        .toFile
        .createIfNotExists(asDirectory = true)
      FolderManipulation.createFolderStructure() should be (false)
    }
  }
}
