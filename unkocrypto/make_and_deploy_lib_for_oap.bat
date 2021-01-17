@echo off

pushd %~dp0
setlocal

set deploydir=test_oap\lib

set gziplib=gzip.jar
set cryptolib=crypto.jar
set mtlib=mt.jar

set bootcp=C:\WTK2.5.2\lib\midpapi20.jar;C:\WTK2.5.2\lib\cldcapi11.jar
set commonflags= -encoding "utf8" -target 1.3 -source 1.3 -bootclasspath %bootcp%


:makegzip
set nextlabel=makecrypto
set additionalflags=
set libname=%gziplib%
set clsdir=gzipclasses
set srcdir=..\gzip-for-openappli\src
set sources=%srcdir%\neetsdkasu\util\zip\Checksum.java ^
    %srcdir%\neetsdkasu\util\zip\CRC32.java ^
    %srcdir%\neetsdkasu\util\zip\Adler32.java

goto buildlib


:makecrypto
set nextlabel=makemtwister
set additionalflags= -cp %deploydir%\%gziplib%
set libname=%cryptolib%
set clsdir=oapclasses
set srcdir=src
set sources=%srcdir%\neetsdkasu\crypto\oap\Crypto.java

goto buildlib


:makemtwister
set nextlabel=endpoint
set additionalflags=
set libname=%mtlib%
set clsdir=mtclasses
set srcdir=..\mt19937ar-MersenneTwister\mt19937ar\src
set sources=%srcdir%\mt19937ar\MTRandom.java

goto buildlib


:buildlib
if not exist %clsdir% mkdir %clsdir%
@echo on

javac ^
    %commonflags% ^
    %additionalflags% ^
    -d %clsdir% ^
    -sourcepath %srcdir% ^
    %sources%

@echo off
if errorlevel 1 goto endpoint
@echo on

@if not exist %deploydir% @mkdir %deploydir%

jar cvf %deploydir%\%libname% -C %clsdir% .

@echo off
if errorlevel 1 goto endpoint
goto %nextlabel%


:endpoint
endlocal
popd