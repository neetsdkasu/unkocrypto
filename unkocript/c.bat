@pushd "%~dp0"
@setlocal
@set dstdir=classes
@set srcdir=src
@set gzipdir=..\gzip-for-openappli\src
@if not exist %dstdir% mkdir %dstdir%

javac ^
    -encoding "utf8" ^
    -d %dstdir% ^
    -sourcepath %srcdir%;%gzipdir% ^
    %srcdir%\neetsdkasu\crypto\Crypto.java

@endlocal
@popd
@if ERRORLEVEL 1 goto :EOF
@echo done.
