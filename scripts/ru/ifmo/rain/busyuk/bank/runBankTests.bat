cd %~dp0
cd ..\..\..\..\..\..\
set junit="lib\*"
set src="java-solutions\ru\ifmo\rain\busyuk\bank"
java -cp %src%\build;%junit% ru.ifmo.rain.busyuk.bank.tests.BankTests
if errorlevel 1 exit \B 1
exit \B 0