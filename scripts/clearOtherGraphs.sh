#!/bin/bash

#federations="diseasome swdf watDiv linkedMDB geoCoordinates watDiv100"

federation=$1
graphs="urn:activitystreams-owl:map http://www.openlinksw.com/schemas/virtrdf# http://www.w3.org/ns/ldp#"

#for federation in ${federations}; do
for i in `seq 0 9`; do
    name=${federation}Endpoint${i}
    pw=passwordep${i}
    portA=$((1111+$i))
    #docker start ${name}
    #sleep 5
    for g in ${graphs}; do
        docker exec -it ${name} isql 1111 -P ${pw} exec="sparql delete { graph <${g}> { ?s ?p ?o } } where { graph <${g}> { ?s ?p ?o } };"
        docker exec -it ${name} isql 1111 -P ${pw} exec="checkpoint;"
    done
    #docker stop ${name}
    #sleep 5
done
#done
