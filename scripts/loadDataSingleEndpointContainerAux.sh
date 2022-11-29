#!/bin/bash

suffix=3030
#federations="diseasome swdf watDiv linkedMDB geoCoordinates watDiv100"

federation=$1
i=30
#for federation in ${federations}; do
#for i in `seq 0 9`; do
    #j=$(($suffix+$i))
    name=${federation}SingleEndpoint
    pw=passwordep${i}
    portA=$((1111+$i))
    #docker start ${name}
    #sleep 5
    docker exec -it ${name} isql 1111 -P ${pw} exec="sparql delete { graph ?g { ?s ?p ?o } } where { graph ?g { ?s ?p ?o } };"
    docker exec -it ${name} isql 1111 -P ${pw} exec="checkpoint;"
    docker exec -it ${name} isql 1111 -P ${pw} exec="LOAD /inputFiles/loadSingleEndpoint.isql"
    #docker stop ${name}
    #sleep 5
#done
#done
