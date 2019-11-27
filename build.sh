#!/bin/bash
# Creates the necessary aliases to use sgit properly.

sbt assembly
export SGIT_HOME=$(pwd)/target/scala-2.13/sgit-assembly-1.0.jar
export sgit='java -jar $SGIT_HOME'

