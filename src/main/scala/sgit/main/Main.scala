package sgit.main

import org.backuity.clist.Cli
import sgit.main.commands._

object Main extends App {
  Cli.parse(args).withProgramName("sgit").withCommands(Init, Status, Diff, Commit, Add) match {
    case Some(Init) => println("init")
    case Some(Status) => println("Status")
    case Some(Diff) => println("Diff")
    case Some(Commit) => println("Commit")
    case Some(Add) => println("Add")
    case None => println("No arguments")
    case _ => println("Error")
  }
}
