@REM GENERATE KEYSTORE FILE FOR DEBUG

@setlocal

@set mykeystore=.\mykeystore

@if exist %mykeystore% ( exit /b )

keytool -genkeypair ^
    -alias neetsdkasu ^
    -keyalg RSA ^
    -keysize 2048 ^
    -keypass ABCDEFGH ^
    -keystore %mykeystore% ^
    -validity 10000 ^
    -storepass ABCDEFGH ^
    -storetype pkcs12 ^
    -dname "CN=NEETSDKASU"


@if exist %mykeystore% (
    keytool -list -keystore %mykeystore% -storepass ABCDEFGH
)

@endlocal
