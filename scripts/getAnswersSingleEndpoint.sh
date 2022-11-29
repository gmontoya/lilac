#!/bin/bash

l=$1
queriesFile=$2
federation=$3
setupFolder=$4
pePort=$5
firstPort=$6
#host=`./getHost.sh $setupFolder/hosts ${pePort} ${firstPort}`
host=localhost
address=http://${host}:${pePort}/sparql
outputFormat=json
answersFolder=$7

queryFile=`mktemp`
tmpFile=`mktemp`

p=`pwd`

./restartSingleEndpointContainer.sh $federation

mkdir -p $answersFolder

for qn in $l; do
    answerFile=$answersFolder/query${qn}
    if [ ! -f ${answerFile} ]; then
        (head -n "$qn" "$queriesFile" | tail -n 1) > $queryFile
        ./getCompleteAnswerSingleEndpoint.sh ${queryFile} ${address} ${outputFormat} ${answerFile}
    fi
done

rm $tmpFile
rm $queryFile

cd ${p}
./stopSingleEndpointContainer.sh $federation
