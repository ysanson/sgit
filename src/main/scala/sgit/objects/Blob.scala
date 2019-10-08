package sgit.objects

case class Blob(content: String, path: String, shaPrint: String) extends TreeObject
