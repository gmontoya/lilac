#!/bin/bash

address=$1
localPort=$2
localProxyPort=$3
tmpFileNR=$4
graph=$5

cd ${fedrahome}/proxy
#echo "starting proxy at address $address with localport $localPort and localproxyport $localProxyPort, using *${tmpFileNR}* to store output"
java -cp .:${httpcomponentsClientPath}/lib/* SingleEndpointProxy2 $address ${localPort} ${localProxyPort} ${graph} > ${tmpFileNR} &
pidProxy=$!
echo $pidProxy
