#!/bin/sh

javadoc -encoding utf8 -d docs -cp ./libs/mt19937ar.jar:./libs/unkocrypto.jar:./classes -exclude neetsdkasu.crypto:mt19937ar -sourcepath ./src/neetsdkasu/fenc/*.java
