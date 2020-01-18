@setlocal
@pushd "%~dp0"
@set dstdir=mclasses
@set srcdir=src\neetsdkasu\
@set bootcp=C:\WTK2.5.2\lib\midpapi20.jar;C:\WTK2.5.2\lib\cldcapi11.jar
@if not exist %dstdir% mkdir %dstdir%
javac ^
    -d %dstdir% ^
    -sourcepath src ^
     -target 1.3 ^
     -source 1.3 ^
     -bootclasspath "%bootcp%" ^
    %srcdir%crypto\Crypto.java
    