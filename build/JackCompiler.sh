#!/bin/sh
cd `dirname $0`
java -classpath "${CLASSPATH}:bin:bin/lib/translators.jar:bin/lib/common.jar" translators.jackCompiler.JackCompiler $1
