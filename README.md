# SGIT
## Git, but in Scala

### Installations instructions
This software is bundled up with _sbt_, which makes it easy to manage dependencies, tests and deployment.

The first step is to download the source code by cloning this repository.

To launch the sbt console, make sure you have java >= 1.8 and sbt installed. The following commands are available:
- `sbt test`: runs the tests and shows the results
- `sbt run`: runs the application.
- `sbt compile`: produces the JVM code for the application.
- `sbt assembly`: produces a JAR file with all the dependencies inside, that can be executed as a standalone application.

## Using the standalone JAR file
When running the `sbt assembly` command, the produced JAR file is created at `/target/scala-2.13/sgit-assembly-xx.jar`, with `xx` depending on the current version.

The JAR archive is executable with the command `java -jar sgit-assembly-xx.jar` in the directory, assuming Java is installed.

However, to avoid copying the jar file everytime we try to create a repository, the next thing to do is adding it with an alias. To achieve this, edit the terminal config file. On Linux/MacOS, edit either `.bashrc`or `.bash_profile`, if using the bash terminal.

In the config file, set an alias:
- `alias sgit='java -jar /path/to/jar/sgit-assembly-xx.jar` on bash
- `Set-Alias -Name sgit -Value java -jar /path/to/jar/sgit-assembly-xx.jar` on Powershell

Please note that the paths are absolute.

Finally, reload the terminal, and you can use sgit everywhere.
