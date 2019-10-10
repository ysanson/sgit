package sgit.objects

case class Commit(name: String, desc: String, parents: Seq[String], files: List[StagedFile]) {

}
