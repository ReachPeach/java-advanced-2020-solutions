cd ..\..\..\..\..\..\..\
set junit="lib\*"
set src="src\ru\ifmo\rain\busyuk\bank"
java -cp %src%\build;%junit% ru.ifmo.rain.busyuk.bank.tests.BankTests
if errorlevel 1 exit 1
exit 0