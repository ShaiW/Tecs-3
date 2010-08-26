#!/bin/sh
java -classpath "$CLASSPATH:.:bin:bin/lib/translators.jar:bin/lib/simulators.jar:bin/lib/common.jar" simulators.VMEmulator.VMEmulatorMain
