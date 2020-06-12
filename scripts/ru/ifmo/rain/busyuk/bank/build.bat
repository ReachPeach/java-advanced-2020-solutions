cd ..\..\..\..\..\..\..\
set junit="lib\*"
set src="src\ru\ifmo\rain\busyuk\bank"
rd /s /q "%src%\build"
mkdir "%src%\build"
javac -d "%src%"\build -cp src;%junit% %src%\client\*.java %src%\common\*.java %src%\server\*.java %src%\tests\*.java
