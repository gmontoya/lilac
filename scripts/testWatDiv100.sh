#!/bin/bash

numClients=10
numQueries=100
availability=0
strategy="FEDERATION" 
firstProxyPort=3130
engines="ANAPSID FedX"
sourceSelectionStrategy="Fedra DAW engine" 
action=justExecute
# justExecute justReplicate all
queriesToExecute=$fedrahome/data/watDiv100Setup/queriesToExecute
pePort=8890
peProxyPort=3100
publicEndpoint=${host}:${peProxyPort}/sparql
setupFolder=$fedrahome/data/watDiv100Setup
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
