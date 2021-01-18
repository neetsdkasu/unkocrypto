#!/bin/sh

echo "1c1
< package neetsdkasu.crypto;
---
> package neetsdkasu.crypto.oap;
11c11,12
< import java.util.zip.Checksum;
---
> import neetsdkasu.util.zip.Checksum;
> import neetsdkasu.crypto.CryptoException;
" | patch -o ./src/neetsdkasu/crypto/oap/Crypto.java ./src/neetsdkasu/crypto/Crypto.java

unix2dos ./src/neetsdkasu/crypto/oap/Crypto.java

