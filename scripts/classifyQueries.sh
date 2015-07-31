#!/bin/bash

queriesFile=$1
queryFile=`mktemp`

#echo "tmpFile: $queryFile"
cd /home/montoya/fedra/code

while read query; do
    echo "$query" > ${queryFile}
    ntp=`java -cp .:/home/montoya/apache-jena-2.11.0/lib/* obtainTriples ${queryFile} | wc -l`
    #echo "ntp: $ntp"
    echo "$query" >> ${queriesFile}_$ntp
    #echo "$query added to ${queriesFile}_$ntp"
done < $queriesFile
