#!/bin/bash

firstPort=$1
last=$2
federation=$3
folder=$fedrahome/data/${federation}
p=`pwd`

for port in `seq $firstPort $last`; do
     cd $fusekiPath
     fuseki-server --port=${port} --update --mem /ds > outputFederationEndpoint${port} &
     pidFE=$!
     sleep 10
     s-put ${host}:${port}/ds/data default $folder/endpoint${port}.nt
     echo $pidFE
done


cd $p

