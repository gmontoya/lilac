#!/bin/bash

port=$1
host=$2
folder=$3

##cd /home/gmontoya/logiciels/apache-jena-fuseki-2.0.0
cd ${fusekiPath}
./fuseki-server --port=${port} --update --mem /ds > outputFederationEndpoint${port} &
###cd ${fusekiHDTPath}
###bin/hdtEndpoint.sh --mem --port=${port} --hdt=${fedrahome}/data/linkedMDBSetup/endpoint${port}.hdt /ds > outputFederationEndpoint${port} &
pidFE=$!
sleep 10
#./bin/s-put ${host}:${port}/ds/data default $folder/endpoint${port}.nt
./s-put ${host}:${port}/ds/data default $folder/endpoint${port}.nt
echo "$host $pidFE"
