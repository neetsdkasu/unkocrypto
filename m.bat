@setlocal
@pushd "%~dp0"
@set dstdir=mclasses
@set srcdir=src
@set bootcp=C:\WTK2.5.2\lib\midpapi20.jar;C:\WTK2.5.2\lib\cldcapi11.jar
@if not exist %dstdir% mkdir %dstdir%
javac ^
    -d %dstdir% ^
    -sourcepath %srcdir% ^
     -target 1.3 ^
     -source 1.3 ^
     -bootclasspath "%bootcp%" ^
    %srcdir%\neetsdkasu\crypto\Crypto.java

@endlocal
@popd
@if ERRORLEVEL 1 goto :EOF
@echo done.