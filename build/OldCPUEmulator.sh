#!/bin/sh
cd `dirname $0`
java -classpath "${CLASSPATH}:bin/oldClasses:bin/lib/OldASM.jar" CPUEmulatorMain $1
