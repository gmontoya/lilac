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
echo "tmpFile: $tmpFile"

for i in `seq 0 $last`; do
    localPort=$(($firstPort+$i))
    localProxyPort=$(($firstProxyPort+$i))
    if [ -n "${graphPrefix}" ]; then
        n=$((${graphIndex}+$i))
        graph="${graphPrefix}${n}"
    else 
        graph=""
    fi
    cd $lilachome/scripts
    #address=`./getHost.sh $lilachome/data/${federation}Setup/hosts $localPort $firstPort`
    address=localhost
    echo $address
    ##oarsh $address "$lilachome/scripts/startOneProxy.sh ${address} ${localPort} ${localProxyPort} ${tmpFile}_$i ${graph}" 
    ./startOneProxy.sh ${address} ${localPort} ${localProxyPort} ${tmpFile}_$i ${graph}
done

if [ -n "${peGraph}" ]; then
    graph="${peGraph}"
else
    graph=""
fi

cd $lilachome/scripts
#address=`./getHost.sh $lilachome/data/${federation}Setup/hosts 3040 $firstProxyPort`
address=localhost
echo $address
##oarsh $address "$lilachome/scripts/startOneProxy.sh ${address} ${pePort} ${peProxyPort} ${tmpFile}_pe"
./startOneProxy.sh ${address} ${pePort} ${peProxyPort} ${tmpFile}_pe

cd $p
