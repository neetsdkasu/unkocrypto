@pushd "%~dp0"
@if /I "%~1"=="all" @call ..\c.bat
@if ERRORLEVEL 1 goto endlabel
@setlocal
@set dstdir=classes
@set srcdir=src
@set mtsrcdir=..\..\mt19937ar-MersenneTwister\mt19937ar\src
@set cpdir=..\classes
@if not exist %dstdir% mkdir %dstdir%

javac ^
    -encoding "utf8" ^
    -d %dstdir% ^
    -sourcepath %srcdir%;%mtsrcdir% ^
    -cp %cpdir% ^
    src\CryptoTest.java

@endlocal
:endlabel
@popd
@if ERRORLEVEL 1 ( @echo error. ) else ( @echo done. )


