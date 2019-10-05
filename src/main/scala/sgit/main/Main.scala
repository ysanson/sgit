package sgit.main

import java.io.File

import org.backuity.clist.Cli
import sgit.main.commands._
import sgit.create.InitializeRepository
import sgit.changes.AddFiles

/**
 * Main entry point of the application.
 * Mainly checks the commands given to the program.
 */
object Main extends App {
  Cli.parse(args).withProgramName("sgit").withCommands(Init, Status, Diff, Commit, Add) match {
    case Some(Init) => InitializeRepository.createFolder()
    case Some(Status) => println("Status")
    case Some(Diff) => println("Diff")
    case Some(Commit) => println("Commit")
    case Some(Add) => AddFiles.add(Add.files)
    case None => println("No arguments")
    case _ => println("Error")
  }
}
