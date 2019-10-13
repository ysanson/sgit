package sgit.tests.changesTest

import org.scalatest.{BeforeAndAfter, FunSpec, Matchers}
import better.files._
import sgit.changes.{AddFiles, CommitFiles}
import sgit.io.{FileManipulation, FolderManipulation}

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
}
