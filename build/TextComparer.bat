@echo off
cd %0\..
java -classpath "%CLASSPATH%;bin;bin/lib/common.jar" common.TextComparer %1 %2
