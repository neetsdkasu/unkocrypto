@echo off

setlocal

pushd %~dp0

set tdir=.\tmpclasses
set ldir=.\libs

if not exist %tdir% ( mkdir %tdir% )
if not exist %ldir% ( mkdir %ldir% )

javac -g:none -d %tdir% ..\mt19937ar-MersenneTwister\mt19937ar\src\mt19937ar\*.java

jar -cvf %ldir%\mt19937ar.jar -C %tdir% mt19937ar

javac -g:none -d %tdir% ..\unkocrypto\src\neetsdkasu\crypto\*.java

jar -cvf %ldir%\unkocrypto.jar -C %tdir% neetsdkasu\crypto

popd

endlocal
