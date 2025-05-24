#!/bin/sh

cd $(dirname $0)

cdir=./classes
ldir=./libs
libs=$ldir/mt19937ar.jar:$ldir/unkocrypto.jar
sdir=./src/neetsdkasu/fenc

mkdir -p $cdir

javac -Werror -cp $libs -d $cdir $sdir/*.java
