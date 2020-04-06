cd ../../../../../../

set res_out=./java-solutions/ru/ifmo/rain/busyuk/implementor/_javadoc
set modulepath=../java-advanced-2020/lib/
set sourcepath=./java-solutions/ru/ifmo/rain/busyuk/implementor/*.java

javadoc %sourcepath% -d %res_out% -p %modulepath% -private -link https://docs.oracle.com/en/java/javase/11/docs/api