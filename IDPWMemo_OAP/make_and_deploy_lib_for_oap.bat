@echo off

pushd %~dp0
setlocal
set oldprompt=%prompt%
prompt $$$S

set deploydir=lib

set gziplib=gzip.jar
set cryptolib=crypto.jar
set mtlib=mt.jar
set utilslib=utils.jar
set memolib=memo.jar

set bootcp=C:\WTK2.5.2\lib\midpapi20.jar;C:\WTK2.5.2\lib\cldcapi11.jar
set commonflags= -g:none -encoding "utf8" -target 1.3 -source 1.3 -bootclasspath %bootcp%


:makegzip
set nextlabel=makecrypto
set additionalflags=
set libname=%gziplib%
set clsdir=gzipclasses
set srcdir=..\gzip-for-openappli\src
set sources=%srcdir%\neetsdkasu\util\zip\Checksum.java ^
    %srcdir%\neetsdkasu\util\zip\CRC32.java

goto buildlib


:makecrypto
set nextlabel=makemtwister
set additionalflags= -cp %deploydir%\%gziplib%
set libname=%cryptolib%
set clsdir=oapclasses
set srcdir=..\unkocrypto\src
set sources=%srcdir%\neetsdkasu\crypto\oap\Crypto.java

goto buildlib


:makemtwister
set nextlabel=makeutils
set additionalflags=
set libname=%mtlib%
set clsdir=mtclasses
set srcdir=..\mt19937ar-MersenneTwister\mt19937ar\src
set sources=%srcdir%\mt19937ar\MTRandom.java

goto buildlib


:makeutils
set nextlabel=makememo
set additionalflags=
set libname=%utilslib%
set clsdir=utilsclasses
set srcdir=..\oap_utils\src
set sources=%srcdir%\neetsdkasu\util\Base64.java

goto buildlib


:makememo
set nextlabel=endpoint
set additionalflags=
set libname=%memolib%
set clsdir=memoclasses
set srcdir=..\IDPWMemo\src
set sources=%srcdir%\idpwmemo\Memo.java 

echo on
mkdir %deploydir%\..\src\idpwmemo
copy %srcdir%\idpwmemo\IDPWMemo.java ^
    %deploydir%\..\src\idpwmemo\
@echo off

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

jar cvf %deploydir%\%libname% -C %clsdir% .

@echo off
if errorlevel 1 goto endpoint
goto %nextlabel%


:endpoint
prompt %oldprompt%
endlocal
popd
echo on
