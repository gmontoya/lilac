#!/bin/bash

setupFolder=$1 
dawIndex=${setupFolder}/dawIndex.ttl
firstPort=$2
last=$3
firstProxyPort=$4
tmpFile=`mktemp`
dawPublicEndpoint=${setupFolder}/publicEndpointDawIndex.ttl
maxNumber=10000
dawFederation=${setupFolder}/dawIndexGenerationTime
dawPE=${setupFolder}/dawPEIndexGenerationTime
p=`pwd`
n=$(($last-$firstPort))

cp $dawPublicEndpoint $dawIndex
cp $dawPE $dawFederation
for i in `seq 0 $n`; do
    port=$(($firstPort+$i))
    proxyPort=$((firstProxyPort+$i))
    cd $fusekiPath/bin
    ./s-query --service http://127.0.0.1:${port}/ds/query 'SELECT * {?s ?p ?o}' --output tsv > $setupFolder/endpoint${port}.nt
    tail -n +2 $setupFolder/endpoint${port}.nt > "$tmpFile" 
    sed 's/$/ ./' "$tmpFile" > $setupFolder/endpoint${port}.nt
    cd $lilachome/scripts
    #address=`./getHost.sh $setupFolder/hosts $port ${firstPort}`
    address=localhost
    host=http://$address
    /usr/bin/time -f "%e" java -Xms3048m -Xmx3048m -cp $dawIndexGeneratorPath/target/FedraDawIndex-1.0-SNAPSHOT.jar DAW.Main $setupFolder/endpoint${port}.nt "${host}:${proxyPort}/ds/sparql" ${maxNumber} >> ${dawIndex} 2> "$tmpFile"
    echo "." >> $dawIndex
    t=`tail -n 1 $tmpFile`
    (echo "$t") >> $dawFederation
done

rm "$tmpFile"
cd $p
source processIndexGenerationTimeFile.sh $dawFederation
