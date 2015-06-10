#!/bin/bash

address=$1
localPort=$2
localProxyPort=$3
tmpFileNR=$4

cd ${fedrahome}/proxy
java -cp .:${httpcomponentsClientPath}/lib/* SingleEndpointProxy2 $address ${localPort} ${localProxyPort} > $tmpFileNR &
pidProxy=$!
echo $pidProxy
