@pushd "%~dp0"
@setlocal
@set dstdir=classes
@set srcdir=src\neetsdkasu\
@if not exist %dstdir% mkdir %dstdir%

javac ^
    -encoding "utf8" ^
    -d %dstdir% ^
    -sourcepath src ^
    %srcdir%crypto\Crypto.java

@endlocal
@popd
@if ERRORLEVEL 1 goto :EOF
@echo done.
