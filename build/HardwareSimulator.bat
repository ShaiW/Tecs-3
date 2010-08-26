@echo off
cd %0\..
java -classpath "%CLASSPATH%;.;bin;bin/lib/simulators.jar;bin/lib/common.jar" simulators.hardwareSimulator.HardwareSimulatorMain %1
