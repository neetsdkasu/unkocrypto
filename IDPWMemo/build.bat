@pushd "%~dp0"
@setlocal
@call c.bat
@if ERRORLEVEL 1 goto endlabel
@set classdir=classes
@set dstdir=bin
@set dstfile=%dstdir%\IDPWMemo.jar
@if not exist %classdir% goto errorlabel
@if not exist %dstdir% mkdir %dstdir%

jar cvfe %dstfile% Main -C %classdir% .

@goto endlabel

:errorlabel

@echo error

:endlabel

@endlocal
@popd
@if ERRORLEVEL 1 goto :EOF
@echo done.
