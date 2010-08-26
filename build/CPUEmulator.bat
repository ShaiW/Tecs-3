@echo off
cd %0\..
java -classpath "%CLASSPATH%;bin;bin/lib/common.jar;bin/lib/translators.jar;bin/lib/simulators.jar" simulators.CPUEmulator.CPUEmulatorMain %1
