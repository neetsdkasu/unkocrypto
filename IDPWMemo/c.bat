@pushd "%~dp0"
@setlocal
@set dstdir=classes
@set srcdir=src
@set mtsrcdir=..\mt19937ar-MersenneTwister\mt19937ar\src
@set cryptsrcdir=..\unkocrypto\src
@if not exist %dstdir% mkdir %dstdir%

javac ^
    -encoding "utf8" ^
    -d %dstdir% ^
    -sourcepath %srcdir%;%mtsrcdir%;%cryptsrcdir% ^
    %srcdir%\Main.java

@endlocal
@popd
@if ERRORLEVEL 1 goto :EOF
@echo done.
