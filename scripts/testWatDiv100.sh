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
queriesToExecute=${fedrahome}/data/watDiv100Setup/queriesToExecute
pePort=8890
peProxyPort=3100
setupFolder=${fedrahome}/data/watDiv100Setup
host=`./getHost.sh $setupFolder/hosts 3040`
host=http://$host
publicEndpoint=${host}:${peProxyPort}/sparql
queries=$setupFolder/queries100_15_500_100_b
firstPort=3030
ldfServer=${host}:5000/watDiv100
federationFile=$setupFolder/federation.ttl
answersFile=$setupFolder/answers
confFile=$setupFolder/confFileWatDiv100
hdtFile=$setupFolder/federationData.hdt
endpointsDescription=$setupFolder/endpointsDescription
updatesFile=$setupFolder/updatesFile

./runAllClients.sh "${strategy}" ${numClients} ${queries} ${numQueries} ${firstPort} ${ldfServer} ${federationFile} ${availability} ${answersFile} ${publicEndpoint} "${engines}" ${confFile} ${hdtFile} ${endpointsDescription} ${updatesFile} ${firstProxyPort} "${sourceSelectionStrategy}" $action "$queriesToExecute" $pePort $peProxyPort $setupFolder
