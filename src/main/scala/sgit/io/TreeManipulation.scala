package sgit.io

import better.files._
import sgit.objects
import sgit.objects.{Blob, Commit, Folder, TreeObject}

import scala.annotation.tailrec

object TreeManipulation {
  /**
   * Creates a tree in memory based on the commit
   * @param baseCommit the commit
   * @return the tree of files.
   */
  def extractTreeFromCommit(baseCommit: Commit): TreeObject = {
    def searchTree(base: String): TreeObject = {
      val fileContent: String = (".sgit/objects/" + base).toFile.contentAsString
      if(fileContent.startsWith("node")) {
        val contents: Array[String] = fileContent.split("\n").tail
        Folder(
          contents.tail.toIndexedSeq.map((item: String) => searchTree(item)),
          contents(0),
          (".sgit/objects/" + base).toFile.name)
      } else Blob(fileContent,
          fileContent.substring(0, fileContent.indexOf('\n')),
          (".sgit/objects/" + base).toFile.name)
    }
    searchTree(baseCommit.tree)
  }

  /**
   * Adds the children of a folder to the tree.
   * @param children the list of children to add
   * @param result the result, a list of treeobjects.
   * @return the list of treeObjects
   */
  @tailrec
  def addChildren(children: Seq[File], result: Seq[TreeObject]): Seq[TreeObject] = {
    if(children.isEmpty) result
    else {
      val first: File = children.head
      if(first.isDirectory) addChildren(children.tail, result:+createTreeForWorkingDir(first).get)
      else {
        val blob: TreeObject = Blob(first.contentAsString, FileManipulation.relativizeFilePath(first).get, first.sha1)
        addChildren(children.tail, blob+:result)
      }
    }
  }

  /**
   * Creates the tree for a given directory.
   * @param baseDir the base directory.
   * @return A tree object representing the root of the tree.
   */
  def createTreeForWorkingDir(baseDir: File): Option[TreeObject] = {
    if(!baseDir.isDirectory) None
    else {
      val children: Seq[TreeObject] = addChildren(baseDir.children.toIndexedSeq, Seq())
      Some(objects.Folder(children, FileManipulation.relativizeFilePath(baseDir).get, baseDir.sha1))
    }
  }
  /**
   * Searches for a specific TreeObject (either a file or a folder)
   * @param tree the base folder to search
   * @param path the path searched
   * @return an option containing the request, or none if not found.
   */
  def searchInTree(tree: TreeObject, path: String): Option[TreeObject] = {
    if(tree.path == path) Some(tree)
    else {
      tree match {
        case folder: Folder =>
          folder.children
            .map(child => searchInTree(child, path))
            .filterNot(result => result.isEmpty)
            .head
        case _ => None
      }
    }
  }
}
