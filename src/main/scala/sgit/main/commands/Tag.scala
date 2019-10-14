package sgit.main.commands

import org.backuity.clist.{Command, arg}

object Tag extends Command(description = "Prints the tags of the repository. If a name is given, creates a new tag.", name = "tag"){
  var tagName = arg[Option[String]](required = false, description = "the tag name, to create a new tag.")
}
