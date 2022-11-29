#!/bin/bash

federation=$1
Federation=$2

setupFolder=$lilachome/data/${federation}Setup
confFile=$setupFolder/confFile

source ${confFile}

#echo "${confFile}"
#echo "$queriesToExecute"

n=$((${numClients}-1))
N=`wc -l $queriesFile | sed 's/^[ ^t]*//' | cut -d' ' -f1`
if [ ! -f "$queriesToExecute" ]; then 
    l=`shuf -i 1-$N -n $numQueries`
    (echo "$l") > ${queriesToExecute}_0_bkp
    echo "queries to executed saved in $queriesToExecute"
    touch "$queriesToExecute"
fi

for i in `seq 1 $n`; do
    rm -f ${queriesToExecute}_$i 
    touch "${queriesToExecute}_$i"
done

l=`cat ${queriesToExecute}_0_bkp`

./getAnswersSingleEndpoint.sh "$l" "$queriesFile" "$federation" "$setupFolder" "$pePort" "$firstPort" "$answersFolder"
