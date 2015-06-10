#!/bin/bash

firstPort=$1
lastPort=$2
firstProxyPort=$3
pePort=$4
peProxyPort=$5
proxyFolder=$6
federation=$7
peGraph="${8}"
graphPrefix="${9}"
graphIndex="${10}"

last=$(($lastPort-$firstPort))
tmpFile=`mktemp`

p=`pwd`
# echo "tmpFile: $tmpFile"

for i in `seq 0 $last`; do
    localPort=$(($firstPort+$i))
    localProxyPort=$(($firstProxyPort+$i))
    if [ -n "${graphPrefix}" ]; then
        n=$((${graphIndex}+$i))
        graph="${graphPrefix}${n}"
    else 
        graph=""
    fi
    cd ${fedrahome}/scripts
    address=`./getHost.sh ${fedrahome}/data/${federation}Setup/hosts $localPort`
    oarsh $address "${fedrahome}/scripts/startOneProxy.sh ${address} ${localPort} ${localProxyPort} ${tmpFile}_$i" 
done

if [ -n "${peGraph}" ]; then
    graph="${peGraph}"
else
    graph=""
fi

cd ${fedrahome}/scripts
address=`./getHost.sh ${fedrahome}/data/${federation}Setup/hosts 3040`
oarsh $address "${fedrahome}/scripts/startOneProxy.sh ${address} ${pePort} ${peProxyPort} ${tmpFile}_pe"

cd $p
