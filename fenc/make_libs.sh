#!/bin/sh

cd $(dirname $0)

tdir=./tmpclasses
ldir=./libs

mkdir -p $tdir
mkdir -p $ldir

javac -Werror -g:none -d $tdir ../mt19937ar-MersenneTwister/mt19937ar/src/mt19937ar/*.java

jar -cvf $ldir/mt19937ar.jar -C $tdir mt19937ar

javac -Werror -g:none -d $tdir ../unkocrypto/src/neetsdkasu/crypto/*.java

jar -cvf $ldir/unkocrypto.jar -C $tdir neetsdkasu/crypto
