@echo off
setlocal
pushd "%~dp0"

set dstdir=libs
set jarfile=libs.jar

set mtdir=..\mt19937ar-MersenneTwister\mt19937ar\src\mt19937ar
set unkodir=..\unkocrypto\src\neetsdkasu\crypto
set idpwmemodir=..\IDPWMemo\src\idpwmemo
set clsdir=%dstdir%\classes

if not exist %clsdir% mkdir %clsdir%

javac -d %clsdir% -encoding utf8 -g:none ^
    %mtdir%\*.java ^
    %unkodir%\*.java ^
    %idpwmemodir%\*.java

jar cvf %dstdir%\%jarfile% -C %clsdir% .

rmdir /S /Q %clsdir%

popd
endlocal
