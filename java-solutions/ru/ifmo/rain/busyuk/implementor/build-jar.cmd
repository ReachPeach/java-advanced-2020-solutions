cd ../../../../../../

set res_out=./java-solutions/ru/ifmo/rain/busyuk/implementor/_build
set classpath=../java-advanced-2020/modules/info.kgeorgiy.java.advanced.implementor
set modulepath=../java-advanced-2020/lib/
set sourcepath=./java-solutions/ru/ifmo/rain/busyuk/implementor/*.java

javac -d %res_out% -cp %classpath% -p %modulepath% %sourcepath%

cd ./java-solutions/ru/ifmo/rain/busyuk/implementor/_build || exit
jar -cf ../_implementor.jar	./ru