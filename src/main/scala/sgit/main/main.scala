package sgit.main

import org.backuity.clist.Cli
import sgit.commands._

object main extends App {
  Cli.parse(args).withCommands(Init, Status, Diff, Commit, Add) match {
    case Some(Init) => println("Init")
    case Some(Status) => println("Status")
    case Some(Diff) => println("Diff")
    case Some(Commit) => println("Commit")
    case Some(Add) => println("Add")
    case None => println("No arguments")
    case _ => println("Error")
  }
}
