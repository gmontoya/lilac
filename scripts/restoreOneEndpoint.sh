#!/bin/bash

port=$1
host=$2
folder=$3

cd $fusekiPath
./fuseki-server --port=${port} --update --mem /ds > outputFederationEndpoint${port} &
pidFE=$!
sleep 10
./s-put ${host}:${port}/ds/data default $folder/endpoint${port}.nt
echo "$host $pidFE"
