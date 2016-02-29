#!/bin/bash

p=`pwd`
cd ${lilachome}/proxy
javac  -cp .:${httpcomponentsClientPath}/lib/* SingleEndpointProxy2.java

cd ${lilachome}/code
javac -cp .:${jenaPath}/lib/* *.java

cd $p
