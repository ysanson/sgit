package sgit.tests.changesTests

import org.scalatest.{BeforeAndAfter, FunSpec, Matchers}
import better.files._
import sgit.changes.{AddFiles, CommitFiles}
import sgit.io.{CommitManipulation, FolderManipulation}


class CommitFilesTest extends FunSpec with BeforeAndAfter with Matchers {

  before{
    if(".sgit".toFile.exists) ".sgit".toFile.delete()
    FolderManipulation.createFolderStructure()
    "test.txt".toFile.createIfNotExists().append("Test")
  }

  after {
    if(".sgit".toFile.exists) ".sgit".toFile.delete()
    if("test.txt".toFile.exists) "test.txt".toFile.delete()
  }

  describe("With no files in the stage") {
    it("Should print an error and exit.") {
      assert(CommitFiles.commit("").isEmpty)
    }
  }

  describe("With files in the stage") {
    describe("And no commits"){
      it("Should return true") {
        AddFiles.add(Seq("test.txt".toFile.toJava))
        assert(CommitFiles.commit("").nonEmpty)
      }
      it("Should create a branch called master which references the commit") {
        AddFiles.add(Seq("test.txt".toFile.toJava))
        val res = CommitFiles.commit("")
        assert(".sgit/refs/heads/master".toFile.exists)
        ".sgit/refs/heads/master".toFile.contentAsString should include (res.get)
      }
      it("Should create a commit file in the objects folder") {
        AddFiles.add(Seq("test.txt".toFile.toJava))
        val res = CommitFiles.commit("")
        ".sgit/objects/".toFile.children.toSeq should have length 2
        assert((".sgit/objects/"+res.get).toFile.exists)
      }
      it("Should be referenced by the last commit method") {
        AddFiles.add(Seq("test.txt".toFile.toJava))
        val res = CommitFiles.commit("")
        CommitManipulation.findMostRecentCommit().get should include (res.get)
      }
      it("Should have one child") {
        AddFiles.add(Seq("test.txt".toFile.toJava))
        CommitFiles.commit("")
        val commit = CommitManipulation.findCommitInfos(CommitManipulation.findMostRecentCommit().get).get
        commit.files should have length 1
        commit.files.head.name should include ("test.txt")
      }
    }
    describe("And a commit that already exists") {
      it("Should have 4 files in the directory (2 commits and 2 files)") {
        AddFiles.add(Seq("test.txt".toFile.toJava))
        CommitFiles.commit("")
        "test.txt".toFile.appendLine("This is another test! :)")
        AddFiles.add(Seq("test.txt".toFile.toJava))
        CommitFiles.commit("")
        ".sgit/objects/".toFile.children.toSeq should have length 4
      }
      it("Should change the head") {
        AddFiles.add(Seq("test.txt".toFile.toJava))
        val first = CommitFiles.commit("")
        "test.txt".toFile.appendLine("This is another test! :)")
        AddFiles.add(Seq("test.txt".toFile.toJava))
        CommitFiles.commit("")
        CommitManipulation.findMostRecentCommit().get should not include (first.get)
      }
      it("Should reference the last commit as a parent") {
        AddFiles.add(Seq("test.txt".toFile.toJava))
        val parent = CommitFiles.commit("")
        "test.txt".toFile.appendLine("This is another test! :)")
        AddFiles.add(Seq("test.txt".toFile.toJava))
        val curr = CommitFiles.commit("")
        val commit = CommitManipulation.findCommitInfos(CommitManipulation.findMostRecentCommit().get).get
        commit.parents.head should include (parent.get)
        commit.name should include (curr.get)
      }
      describe("And a file with the same name"){
        it("Should have only one file referenced"){
          AddFiles.add(Seq("test.txt".toFile.toJava))
          CommitFiles.commit("")
          "test.txt".toFile.appendLine("This is another test! :)")
          AddFiles.add(Seq("test.txt".toFile.toJava))
          CommitFiles.commit("")
          val commit = CommitManipulation.findCommitInfos(CommitManipulation.findMostRecentCommit().get).get
          commit.files should have length 1
          commit.files.head.name should include ("test.txt")
        }
      }
      describe("And another file added"){
        it("Should have 2 files referenced") {
          AddFiles.add(Seq("test.txt".toFile.toJava))
          CommitFiles.commit("")
          "another.txt".toFile.createIfNotExists().appendLine("This is another file.")
          AddFiles.add(Seq("another.txt".toFile.toJava))
          CommitFiles.commit("")
          val commit = CommitManipulation.findCommitInfos(CommitManipulation.findMostRecentCommit().get).get
          commit.files should have length 2
          commit.files.head.name should include ("test.txt")
          commit.files.tail.head.name should include ("another.txt")
          "another.txt".toFile.delete()
        }
      }
    }
  }

}
