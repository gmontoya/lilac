#!/bin/bash

strategies=$1
numberClients=$2
queriesFile=$3
numberQueries=$4
firstPort=$5
answersFolder=$6
engines=$7
hdtFile=$8
firstProxyPort=$9
sourceSelectionStrategy=${10}
setupFolder=${11}
federation=${12}
queriesToExecute=${13}
lastPort=$(($firstPort+$numberClients-1))

p=`pwd`
n=$((${numberClients}-1))
N=`wc -l $queriesFile | sed 's/^[ ^t]*//' | cut -d' ' -f1`
if [ ! -f "$queriesToExecute" ]; then 
    f=`mktemp`
    queriesToExecute=`echo ${f##*/}`
    l=`shuf -i 1-$N -n $numberQueries`
    (echo "$l") > ${queriesToExecute}_0_bkp
    echo "queries to executed saved in $queriesToExecute"
    touch "$queriesToExecute"
fi

for i in `seq 1 $n`; do
    touch "${queriesToExecute}_$i"
done

l=`cat ${queriesToExecute}_0_bkp`
./produceAnswer.sh "$l" "$queriesFile" "$hdtFile" "${firstProxyPort}" "$answersFolder"

sed -i".bkp" "s,queriesToExecute=[0-9a-zA-Z_/\.\$]*,queriesToExecute=${queriesToExecute}," test${federation}.sh

n=0
while [ "$n" -lt "$numberQueries" ]; do
    cd ${p}
    n=$(($n+1))
    q=`head -n $n ${queriesToExecute}_0_bkp | tail -n 1`
    (echo "$q") > ${queriesToExecute}_0
    ./test${federation}.sh
    for strategy in $strategies; do
        for engine in $engines; do
            for ss in $sourceSelectionStrategy; do    
                label="$engine${ss}$strategy"
                f=${setupFolder}/output${label}${numberClients}Client
                x=`./getIRNCFederation.sh ${f} ${firstPort} ${lastPort}`
                y=`grep "query${q}" ${f}${firstPort}`
                z="$y $x"
                (echo "$z") >> ${f}
            done
        done
    done
done

