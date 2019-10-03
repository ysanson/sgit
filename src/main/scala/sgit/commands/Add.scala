package sgit.commands

import org.backuity.clist.{Command, args}

object Add extends Command(description = "Adds a file or a group of files to the version control.", name = "add") {
  var files = args[Seq[String]](description = "Files to add, either a filename, a dot (.), a regex")
}
