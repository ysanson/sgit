package sgit.main

import org.backuity.clist.Cli
import sgit.commands._

object main extends App {
  Cli.parse(args).withCommands(Init, Status) match {
    case Some(Init) => println("Init")
    case None => println("No arguments")
    case _ => println("Error")
  }
}
