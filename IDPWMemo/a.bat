@pushd "%~dp0"

java -cp "classes" Main %* 

@popd
