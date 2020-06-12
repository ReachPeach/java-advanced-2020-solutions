cd ..\..\..\..\..\..\..\lib\
set src="..\java-solutions\ru\ifmo\rain\busyuk\bank"
java -jar junit-platform-console-standalone-1.7.0-M1.jar -cp %src%\build\ --scan-classpath
if errorlevel 1 exit 1
exit 0