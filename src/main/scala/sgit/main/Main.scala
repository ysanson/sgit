package sgit.main

import org.backuity.clist.Cli
import sgit.changes.{AddFiles, CommitFiles, Differences}
import sgit.create.InitializeRepository
import sgit.main.commands.{Add, Commit, Diff, Init, Status}

/**
 * Main entry point of the application.
 * Mainly checks the commands given to the program.
 */
object Main extends App {
  Cli.parse(args).withProgramName("sgit").withCommands(Init, Status, Diff, Commit, Add) match {
    case Some(Init) => InitializeRepository.createFolder()
    case Some(Status) => Differences.status()
    case Some(Diff) => println("Diff")
    case Some(Commit) => CommitFiles.commit()
    case Some(Add) => AddFiles.add(Add.files)
    case None => println("No arguments")
    case _ => println("Error")
  }
}
