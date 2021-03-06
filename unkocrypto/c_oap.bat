@setlocal
@pushd "%~dp0"
@set dstdir=oapclasses
@set srcdir=src
@set gzipdir=..\gzip-for-openappli\src
@set bootcp=C:\WTK2.5.2\lib\midpapi20.jar;C:\WTK2.5.2\lib\cldcapi11.jar
@if not exist %dstdir% mkdir %dstdir%
javac ^
    -encoding "utf8" ^
    -d %dstdir% ^
    -sourcepath %srcdir%;%gzipdir% ^
    -target 1.3 ^
    -source 1.3 ^
    -bootclasspath "%bootcp%" ^
    %srcdir%\neetsdkasu\crypto\oap\Crypto.java

@endlocal
@popd
@if ERRORLEVEL 1 goto :EOF
@echo done.