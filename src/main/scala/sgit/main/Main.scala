package sgit.main

import org.backuity.clist.Cli
import sgit.changes.{AddFiles, CommitFiles, WorkspaceStatus, Restore}
import sgit.create.InitializeRepository
import sgit.logs.Logs
import sgit.refs.{Tags, Branches}
import sgit.main.commands.{Add, Commit, Diff, Init, Status, Log, Tag, Branch, Checkout}

/**
 * Main entry point of the application.
 * Mainly checks the commands given to the program.
 */
object Main extends App {
  Cli.parse(args)
    .withProgramName("sgit")
    .version("1.0")
    .withCommands(Init, Status, Diff, Commit, Add, Log, Tag, Branch, Checkout) match {
    case Some(Init) => InitializeRepository.createFolder()
    case Some(Status) => WorkspaceStatus.status()
    case Some(Diff) => println("This function is not implemented.")
    case Some(Commit) => CommitFiles.commit(Commit.desc)
    case Some(Add) => AddFiles.add(Add.files)
    case Some(Log) => Logs.showLog(Log.overtime, Log.stat)
    case Some(Tag) => Tags.handleTag(Tag.tagName)
    case Some(Branch) => Branches.handleBranches(Branch.branchName, Branch.verbose)
    case Some(Checkout) => Restore.checkout(Checkout.commitName)
    case None => println("No arguments")
    case _ => println("Error")
  }
}
