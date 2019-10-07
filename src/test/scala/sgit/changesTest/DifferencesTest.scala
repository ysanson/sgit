package sgit.changesTest

import org.scalatest._
import sgit.changes.Differences
import better.files._
import sgit.create.InitializeRepository

class DifferencesTest extends FunSpec with BeforeAndAfter {
  before{
    InitializeRepository.createFolder()
  }
  after {
    val _ : File = ".sgit"
      .toFile
      .delete()
  }

  describe("With a given folder") {
    it("Should list all subfolders.") {
      val res = Differences.findDifferentFiles(".".toFile)
      assert(res.isEmpty)
    }
  }
}
