package sgit.ioTests

import org.scalatest._
import java.nio.file.{Files, Paths}
import sgit.io.FolderManipulation
import better.files._

class FolderManipulationTests extends FunSpec {
  describe("With no folder"){
    it("Should create the folder structure"){
      val _ : File = ".sgit"
        .toFile
        .delete()
      val res = FolderManipulation.createFolderStructure()
      assert(res)
      assert(Files.exists(Paths.get(".sgit/refs/heads")))
      assert(Files.exists(Paths.get(".sgit/refs/tags")))
      assert(Files.exists(Paths.get(".sgit/objects")))
      assert(Files.exists(Paths.get(".sgit/staged")))
      assert(Files.readString(Paths.get(".sgit/HEAD")) == "ref: refs/heads/master")
      val _ : File = ".sgit"
        .toFile
        .delete()
    }
  }
}
