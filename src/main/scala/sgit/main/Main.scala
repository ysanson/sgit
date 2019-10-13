package sgit.main

import org.backuity.clist.Cli
import sgit.changes.{AddFiles, CommitFiles, Status}
import sgit.create.InitializeRepository
import sgit.logs.Logs
import sgit.main.commands.{Add, Commit, Diff, Init, Status, Log}

/**
 * Main entry point of the application.
 * Mainly checks the commands given to the program.
 */
object Main extends App {
  Cli.parse(args).withProgramName("sgit").withCommands(Init, Status, Diff, Commit, Add, Log) match {
    case Some(Init) => InitializeRepository.createFolder()
    case Some(Status) => Status.status()
    case Some(Diff) => println("Diff")
    case Some(Commit) => CommitFiles.commit(Commit.desc)
    case Some(Add) => AddFiles.add(Add.files)
    case Some(Log) => Logs.showLog(Log.overtime, Log.stat)
    case None => println("No arguments")
    case _ => println("Error")
  }
}
