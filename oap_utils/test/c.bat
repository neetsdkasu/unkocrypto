@pushd "%~dp0"
@setlocal
@set dstdir=classes
@set srcdir=src
@set utilsrcdir=..\src
@if not exist %dstdir% mkdir %dstdir%

javac ^
    -encoding "utf8" ^
    -d %dstdir% ^
    -sourcepath %srcdir%;%utilsrcdir% ^
    src\UtilTest.java

@endlocal
:endlabel
@popd
@if ERRORLEVEL 1 ( @echo error. ) else ( @echo done. )


