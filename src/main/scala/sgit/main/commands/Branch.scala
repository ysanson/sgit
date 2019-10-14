package sgit.main.commands

import org.backuity.clist.{Command, arg}

object Branch extends Command (description = "Create or list branches.", name = "branch"){
  var branchName = arg[Option[String]](required = false, description = "If given, creates a new branch.")
}
