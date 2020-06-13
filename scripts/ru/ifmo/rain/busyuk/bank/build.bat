cd %~dp0
cd ..\..\..\..\..\..\
set junit=lib\*
set src=java-solutions\ru\ifmo\rain\busyuk\bank
rd /s /q build
mkdir build
javac -d build -cp java-solutions;%junit% %src%\client\*.java %src%\common\*.java %src%\server\*.java %src%\tests\*.java
