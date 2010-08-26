@echo off
cd %0\..
java -classpath "%CLASSPATH%;.;bin;bin/lib/translators.jar;bin/lib/simulators.jar;bin/lib/common.jar" simulators.VMEmulatr.VMEmulatorMain %1
