package sgit.io

import better.files._
import sgit.objects._

object TreeManipulation {
  /**
   * Creates a tree in memory based on the commit
   * @param baseCommit the commit
   * @return the tree of files.
   */
  def extractTree(baseCommit: Commit): TreeObject = {
    def searchTree(base: String): TreeObject = {
      val fileContent: String = (".sgit/objects/" + base).toFile.contentAsString
      if(fileContent.startsWith("node")) {
        val contents: Array[String] = fileContent.split("\n").tail
        Folder(
          contents.tail.map((item: String) => searchTree(item)),
          contents(0))
      } else Blob(fileContent, fileContent.substring(0, fileContent.indexOf('\n')))
    }
    searchTree(baseCommit.tree)
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
        case folder: Folder => {
          folder.children
            .map(child => searchInTree(child, path))
            .filterNot(result => result.isEmpty)
            .head
        }
        case _ => None
      }
    }
  }
}
