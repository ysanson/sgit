package sgit.main.commands

import org.backuity.clist.{Command, arg, opt}

object Branch extends Command (description = "Create or list branches.", name = "branch"){
  var branchName = arg[Option[String]](required = false, description = "If given, creates a new branch.")
  var verbose = opt[Boolean](default = false, name = "verbose", description = "Prints the last commit info on each branch.", abbrev = "v")
}
