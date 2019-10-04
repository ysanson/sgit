package sgit.main.commands

import java.io.File

import org.backuity.clist.{Command, args, opt}

object Add extends Command(description = "Adds a file or a group of files to the version control.", name = "add") {
  var files = args[Seq[File]](description = "Files to add, either a filename, a dot (.), a regex")
}
