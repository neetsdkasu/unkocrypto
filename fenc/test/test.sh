#!/bin/sh

cd $(dirname $0)

cdir=../classes
ldir=../libs
libs=$ldir/mt19937ar.jar:$ldir/unkocrypto.jar:$cdir
tcdir=./classes

mkdir -p $tcdir

javac -Werror -cp $libs -d $tcdir Test.java && java -cp $libs:$tcdir Test
