package sgit.objects

case class Blob(content: String, path: String) extends TreeObject
