#!/bin/bash

firstPort=$1
lastPort=$2
firstProxyPort=$3
pePort=$4
peProxyPort=$5
proxyFolder=$6
addressA=$7
peGraph="${8}"
graphPrefix="${9}"
graphIndex="${10}"

last=$(($lastPort-$firstPort))
tmpFile=`mktemp`

p=`pwd`
echo "tmpFile: $tmpFile"

cd $proxyFolder

for i in `seq 0 $last`; do
    localPort=$(($firstPort+$i))
    localProxyPort=$(($firstProxyPort+$i))
    if [ -n "${graphPrefix}" ]; then
        n=$((${graphIndex}+$i))
        graph="${graphPrefix}${n}"
    else 
        graph=""
    fi
    java -cp .:/home/montoya/proxy/httpcomponents-client-4.3.5/lib/* SingleEndpointProxy2 ${addressA} ${localPort} ${localProxyPort} $graph > ${tmpFile}_$i &
    pidProxy=$!
    echo "$pidProxy"
done

if [ -n "${peGraph}" ]; then
    graph="${peGraph}"
else
    graph=""
fi

java -cp .:/home/montoya/proxy/httpcomponents-client-4.3.5/lib/* SingleEndpointProxy2 ${addressA} ${pePort} ${peProxyPort} $graph > ${tmpFile}_pe &
pidProxy=$!
echo "$pidProxy"

cd $p
