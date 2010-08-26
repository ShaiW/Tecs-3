#!/bin/sh
cd `dirname $0`
java -classpath "$CLASSPATH:bin:bin/lib/common.jar:bin/lib/translators.jar" translators/syntaxAnalyzer/Private/SyntaxAnalyzer $1 $2