#!/bin/sh

mkdir -p -v IDPWMemo_OAP/src/idpwmemo

echo "6,7c6,7
< import java.util.zip.Checksum;
< import java.util.zip.CRC32;
---
> import neetsdkasu.util.zip.Checksum;
> import neetsdkasu.util.zip.CRC32;
10c10
< import neetsdkasu.crypto.Crypto;
---
> import neetsdkasu.crypto.oap.Crypto;
25c25,30
<     private final MTRandom rand = new MTRandom();
---
>     private final MTRandom rand = new MTRandom() {
>         public int nextInt()
>         {
>             return next(32); // In OpenAppli-JavaMachine-java.util.Random, nextInt() method does not use next(int) method...
>         }
>     };
" | patch -o ./Cryptor.java ./IDPWMemo/src/idpwmemo/Cryptor.java

unix2dos ./Cryptor.java

mv ./Cryptor.java ./IDPWMemo_OAP/src/idpwmemo/

