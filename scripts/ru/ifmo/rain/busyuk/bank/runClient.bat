cd %~dp0
cd ..\..\..\..\..\..
set src="java-solutions\ru\ifmo\rain\busyuk\bank"
java -cp %src%\build ru.ifmo.rain.busyuk.bank.client.Client @%1 %2 %3 %4 %5