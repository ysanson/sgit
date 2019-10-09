package sgit.objects

case class Folder(children: Seq[TreeObject], path: String, shaPrint: String) extends TreeObject
