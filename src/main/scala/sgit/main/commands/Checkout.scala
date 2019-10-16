package sgit.main.commands

import org.backuity.clist.{Command, arg}

object Checkout extends Command(description = "Checks out a commit, a branch or a tag.", name = "checkout") {
  var commitName = arg[String](required = true, description = "A commit hash, a branch or a tag name.")
}
