#!/bin/bash

numClients=10
numQueries=100
availability=0
strategy="FEDERATION"
firstProxyPort=3130
engines="FedX ANAPSID" 
sourceSelectionStrategy="Fedra DAW engine"
action=justExecute
# justExecute justReplicate all
queriesToExecute=$fedrahome/data/linkedMDBSetup/queriesToExecute
pePort=8999
peProxyPort=3100
host=`./getHost.sh 3040`
host=http://$host
publicEndpoint=${host}:${peProxyPort}/sparql
setupFolder=$fedrahome/data/linkedMDBSetup
queries=$setupFolder/queries
firstPort=3030
federation=linkedMDB
ldfServer=${host}:5000/${federation}
federationFile=$setupFolder/federation.ttl
answersFile=$setupFolder/answers/
confFile=$setupFolder/confFileLinkedMDB
hdtFile=$setupFolder/federationData.hdt
endpointsDescription=$setupFolder/endpointsDescription
updatesFile=$setupFolder/updatesFile

./runAllClients.sh "${strategy}" ${numClients} ${queries} ${numQueries} ${firstPort} ${ldfServer} ${federationFile} ${availability} ${answersFile} ${publicEndpoint} "${engines}" ${confFile} ${hdtFile} ${endpointsDescription} ${updatesFile} ${firstProxyPort} "${sourceSelectionStrategy}" $action "$queriesToExecute" $pePort $peProxyPort $setupFolder
