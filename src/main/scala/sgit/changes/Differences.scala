package sgit.changes

import better.files._
import sgit.io._
import sgit.objects._

object Differences {
  /**
   * Searches for the files that have been modified, added or deleted.
   * It starts from a base folder and searches all the way through.
   *
   * @param baseFolder the base folder to search from
   * @return a sequence of files that have been modified, or None if error.
   */
  def findDifferentFiles(baseFolder: File): Option[Seq[File]] = {
    if (!baseFolder.isDirectory) return None

    val content: Option[List[File]] = FolderManipulation.listSubDirectories(baseFolder)
    if (content.isEmpty) return None
    println(content)
    val commit: Option[String] = CommitManipulation.findMostRecentCommit()

    if (commit.isEmpty) return None

    val currentCommit: Commit = CommitManipulation.findCommitInfos(commit.get)
    val tree: TreeObject = TreeManipulation.extractTree(currentCommit)

    val diffs = content.get
      .flatMap(file => {
        val obj: Option[TreeObject] =
          TreeManipulation.searchInTree(tree, FileManipulation.relativizeFilePath(file).get)
        if (obj.isEmpty) return null
        obj.get match {
          case _: Blob => null
          case folder: Folder => FolderManipulation.findDifferentFilesForFolder(file, folder).orNull
        }
      })
      .filterNot(file => file == null)
    Some(diffs)
  }
}
