#!/bin/sh

cd $(dirname $0)

cdir=./classes
ldir=./libs
libs=$ldir/mt19937ar.jar:$ldir/unkocrypto.jar:$cdir
excl=neetsdkasu.crypto:mt19937ar
srcs=./src/neetsdkasu/fenc/*.java

javadoc -encoding utf8 -d docs -cp $libs -exclude $excl -sourcepath $srcs
