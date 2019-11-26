package sgit.main.commands

import org.backuity.clist.{Command, opt}

object Log extends Command (name = "log", description = "Shows a log of the commits."){
  var overtime: Boolean = opt[Boolean](default = false, description = "Shows the changes over time", name = "p", abbrev = "p")
  var stat: Boolean= opt[Boolean](default = false, description = "Shows the stats of each commit", name = "stat")
}
