cd %~dp0
cd ..\..\..\..\..\..\
set junit="lib\*"
java -cp build;%junit% ru.ifmo.rain.busyuk.bank.tests.BankTests
if errorlevel 1 exit /B 1
exit /B 0