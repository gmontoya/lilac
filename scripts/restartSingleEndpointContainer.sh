#!/bin/bash

federation=$1
i=10
graphs="urn:activitystreams-owl:map http://www.openlinksw.com/schemas/virtrdf# http://www.w3.org/ns/ldp#"

name=${federation}SingleEndpoint
docker start ${name}

sleep 30

pw=passwordep${i}

for g in ${graphs}; do
    docker exec -it ${name} isql 1111 -P ${pw} exec="sparql delete { graph <${g}> { ?s ?p ?o } } where { graph <${g}> { ?s ?p ?o } };"
    docker exec -it ${name} isql 1111 -P ${pw} exec="checkpoint;"
done

sleep 30
