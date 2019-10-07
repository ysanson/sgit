package sgit.ioTests

import org.scalatest._
import java.nio.file.{Files, Paths}
import sgit.io.FolderManipulation
import better.files._

class FolderManipulationTests extends FunSpec with BeforeAndAfter {

  after {
    val _ : File = ".sgit"
      .toFile
      .delete()
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
      assert(Files.exists(Paths.get(".sgit/refs/heads")))
      assert(Files.exists(Paths.get(".sgit/refs/tags")))
      assert(Files.exists(Paths.get(".sgit/objects")))
      assert(Files.exists(Paths.get(".sgit/staged")))
      assert(Files.readString(Paths.get(".sgit/HEAD")) == "ref: refs/heads/master")

    }
  }

  describe("With a folder already in place"){
    it("Should do nothing"){
      val _ : File = ".sgit"
        .toFile
        .createIfNotExists(asDirectory = true)
      assert(!FolderManipulation.createFolderStructure())
    }
  }
}