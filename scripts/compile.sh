#!/bin/bash

p=`pwd`
cd ${fedrahome}/proxy
javac  -cp .:${httpcomponentsClientPath}/lib/* SingleEndpointProxy2.java

cd ${fedrahome}/code
javac -cp .:${jenaPath}/lib/* *.java

cd $p
