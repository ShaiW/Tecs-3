#!/bin/sh
cd `dirname $0`
java -classpath "$CLASSPATH:bin:bin/lib/translators.jar:bin/lib/common.jar:bin/lib/simulators.jar" translators.assembler.HackAssembler $1
