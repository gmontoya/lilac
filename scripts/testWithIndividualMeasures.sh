#!/bin/bash

federation=$1
Federation=$2
action="$4"
queriesToExecute=${3}
strategies="FEDERATION"
numberClients=10
numberQueries=3
# options: FedX ANAPSID
engines="FedX"
# options: LILAC Fedra DAW engine
sourceSelectionStrategy="engine LILAC"
firstPort=8890
setupFolder=$lilachome/data/${federation}Setup
#outputFolder=$lilachome/output${Federation}
lastPort=$(($firstPort+$numberClients-1))

#mkdir -p ${outputFolder}


n=0
while [ "$n" -lt "$numberQueries" ]; do
    n=$(($n+1))
    q=`head -n $n ${queriesToExecute}_0_bkp | tail -n 1`
    (echo "$q") > ${queriesToExecute}_0
    ./runTest.sh ${federation} ${Federation} "$strategies" "$engines" "$sourceSelectionStrategy" "$action" $numberClients $numberQueries $firstPort
    for strategy in $strategies; do
        for engine in $engines; do
            for ss in $sourceSelectionStrategy; do    
                label="$engine${ss}${strategy}"
                #f=${outputFolder}/output${label}${numberClients}Client
		f=${setupFolder}/output${label}${numberClients}Client
                if [ ! -f "${f}" ]; then
                    echo "# query, executionTime, cpuSecondsSystem, cpuSecondsProcess, %CPU, avgResidentSetSizeProcess, maxResidentSetSizeProcess, completeness, soundness, numberSelectedSources, numberTriples, shape, transferredTuples, numberCalls" >> ${f}
                fi
                x=`./getIRNCFederation.sh ${f} ${firstPort} ${lastPort}`
                y=`grep "query${q}" ${f}${firstPort}`
                z="$y $x"
                (echo "$z") >> ${f}
            done
        done
    done
done
