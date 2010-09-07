@echo off
cd %0\..
java -classpath "%CLASSPATH%;bin/OldClasses;bin/lib/OldASM.jar" CPUEmulatorMain %1
