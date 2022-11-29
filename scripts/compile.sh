#!/bin/bash

p=`pwd`
cd $lilachome/proxy
javac  -cp .:$httpcomponentsClientPath/* SingleEndpointProxy2.java

cd $lilachome/code
javac -cp .:$jenaPath/lib/* *.java

cd $p
