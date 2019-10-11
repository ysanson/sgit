package sgit.objects
import java.util.Date

case class CommitObject(name: String, desc: String, parents: Seq[String], time: Date, files: List[StagedFile]) {
}
