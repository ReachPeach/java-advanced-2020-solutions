cd %~dp0
cd ..\..\..\..\..\..\lib\
set src="..\java-solutions\ru\ifmo\rain\busyuk\bank"
java -jar junit-platform-console-standalone-1.7.0-M1.jar -cp ..\build\ --scan-classpath
if errorlevel 1 exit /B 1
exit /B 0