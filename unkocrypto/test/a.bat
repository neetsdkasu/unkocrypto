@pushd "%~dp0"

java -cp "classes;..\classes" CryptoTest %* 

@popd
