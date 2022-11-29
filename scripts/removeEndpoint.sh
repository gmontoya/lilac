#!/bin/bash
# removes one endpoint from the federation
# used to remove the public endpoint if unavailable

federationDescription=$1
endpoint=$2

query="CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . ?s <http://fluidops.org/config#SPARQLEndpoint> ?e . FILTER (?e != \"$endpoint\") }"

#echo "$query"

cd $jenaPath/bin

./sparql --data ${federationDescription} --results=Turtle "$query"
