package sgit.main.commands

import org.backuity.clist._

object Commit extends Command (description = "Commits the staged changes", name = "commit"){
  var desc: String = opt[String](default= "Placeholder description", name="message")
}
