#!/bin/sh 

dstdir="./libs"
jarfile="libs.jar"

mtdir="../mt19937ar-MersenneTwister/mt19937ar/src/mt19937ar"
unkodir="../unkocrypto/src/neetsdkasu/crypto"
idpwmemodir="../IDPWMemo/src/idpwmemo"
clsdir="$dstdir/classes"

mkdir -p $clsdir

javac -d $clsdir -encoding utf8 -g:none $mtdir/*.java $unkodir/*.java $idpwmemodir/*.java

jar cvf $dstdir/$jarfile -C $clsdir .

rm -r $clsdir

