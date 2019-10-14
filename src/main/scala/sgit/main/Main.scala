package sgit.main

import org.backuity.clist.Cli
import sgit.changes.{AddFiles, CommitFiles, WorkspaceStatus}
import sgit.create.InitializeRepository
import sgit.logs.Logs
import sgit.branches.Tags
import sgit.main.commands.{Add, Commit, Diff, Init, Status, Log, Tag}

/**
 * Main entry point of the application.
 * Mainly checks the commands given to the program.
 */
object Main extends App {
  Cli.parse(args)
    .withProgramName("sgit")
    .version("0.5")
    .withCommands(Init, Status, Diff, Commit, Add, Log, Tag) match {
    case Some(Init) => InitializeRepository.createFolder()
    case Some(Status) => WorkspaceStatus.status()
    case Some(Diff) => println("Diff")
    case Some(Commit) => CommitFiles.commit(Commit.desc)
    case Some(Add) => AddFiles.add(Add.files)
    case Some(Log) => Logs.showLog(Log.overtime, Log.stat)
    case Some(Tag) => Tags.handleTag(Tag.tagName)
    case None => println("No arguments")
    case _ => println("Error")
  }
}
