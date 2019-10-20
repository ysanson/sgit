package sgit.io

import better.files._
import sgit.objects
import sgit.objects.{Blob, CommitObject, Folder, TreeObject}

import scala.annotation.tailrec

object TreeManipulation {
  /**
   * Creates a tree in memory based on the commit
   * @param baseCommit the commit
   * @return the tree of files.
   */
  def extractTreeFromCommit(baseCommit: CommitObject): TreeObject = {
    def searchTree(base: String): TreeObject = {
      val fileContent: String = (".sgit/objects/" + base).toFile.contentAsString
      if(fileContent.startsWith("node")) {
        val contents: Array[String] = fileContent.split("\n").tail
        Folder(
          contents.tail.toIndexedSeq.map((item: String) => searchTree(item)),
          contents(0),
          (".sgit/objects/" + base).toFile.name)
      } else Blob(fileContent,
          fileContent.substring(0, fileContent.indexOf('\n')).replace("\\", "/"),
          (".sgit/objects/" + base).toFile.name)
    }
    searchTree(baseCommit.name)
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
        val blob: TreeObject = Blob(first.contentAsString,
          FileManipulation.relativizeFilePath(first).get.replace("\\", "/"),
          first.sha1)
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
   * Finds an element in an array of children.
   * If none is found, returns None.
   * If a folder is found, searches in the folder.
   * @param elements the elements to search in.
   * @param path The path to search.
   * @return An optional treeObject
   */
  @tailrec
  def findInChildren(elements: Seq[TreeObject], path: String): Option[TreeObject] = {
    if(elements.isEmpty) None
    else {
      val firstElt: TreeObject = elements.head
      if (firstElt.isInstanceOf[Folder]) searchInTree(firstElt, path)
      else if (firstElt.path == path) Some(firstElt)
      else findInChildren(elements.tail, path)
    }
  }

  /**
   * Searches in the tree for an object. If a folder is found, calls findInChildren to explore the children.
   * If None is found, returns None.
   * @param tree the tree element to search.
   * @param path the path to search.
   * @return An optional TreeObject.
   */
  def searchInTree(tree: TreeObject, path: String): Option[TreeObject] = {
    if(tree.path == path) Some(tree)
    else {
      tree match{
        case folder: Folder => findInChildren(folder.children, path)
        case _ => None
      }
    }
  }

  /**
   * Checks if the path exists in the directory.
   * @param tree the tree object to search in.
   * @param path The path to search for.
   * @return True if found, false otherwise.
   */
  def existsInTree(tree: TreeObject, path: String): Boolean = {
    val elt = searchInTree(tree, path)
    if(elt.nonEmpty) true
    else false
  }

}
