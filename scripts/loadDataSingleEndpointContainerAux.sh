#!/bin/bash

federation=$1
i=10

name=${federation}SingleEndpoint
pw=passwordep${i}
portA=$((1111+$i))

docker exec -it ${name} isql 1111 -P ${pw} exec="sparql delete { graph ?g { ?s ?p ?o } } where { graph ?g { ?s ?p ?o } };"
docker exec -it ${name} isql 1111 -P ${pw} exec="checkpoint;"
docker exec -it ${name} isql 1111 -P ${pw} exec="LOAD /inputFiles/loadSingleEndpoint.isql"
