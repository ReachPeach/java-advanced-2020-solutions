set artifactsPath="../../../../../../../java-advanced-2020/artifacts"
set inKgeorgiyJarClassPath="info/kgeorgiy/java/advanced/implementor"
set jarClassPath="ru/ifmo/rain/busyuk/implementor"

javac -d ./ ../Implementor.java ../JarImplementor.java
jar cf ../_implemetor.jar %jarClassPath%/Implementor.class %jarClassPath%/JarImplementor.class %inKgeorgiyJarClassPath%/Impler.class %inKgeorgiyJarClassPath%/JarImpler.class %inKgeorgiyJarClassPath%/ImplerException.class