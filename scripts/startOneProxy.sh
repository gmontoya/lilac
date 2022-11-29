#!/bin/bash

address=$1
localPort=$2
localProxyPort=$3
tmpFileNR=$4
graph=$5

cd $lilachome/proxy
#echo "starting proxy at address $address with localport $localPort and localproxyport $localProxyPort, using *${tmpFileNR}* to store output"
java -cp .:$httpcomponentsClientPath/* SingleEndpointProxy2 $address ${localPort} ${localProxyPort} ${graph} > ${tmpFileNR} &
pidProxy=$!
echo $pidProxy
