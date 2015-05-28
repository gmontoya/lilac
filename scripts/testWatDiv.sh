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
queriesToExecute=$fedrahome/data/watDivSetup/queriesToExecute
pePort=8890
peProxyPort=3100
host=`./getHost.sh 3040`
host=http://$host
publicEndpoint=${host}:${peProxyPort}/sparql
setupFolder=$fedrahome/data/watDivSetup
queries=$setupFolder/queries15_500_100_b
firstPort=3030
ldfServer=${host}:5000/watDiv1
federationFile=$setupFolder/federation.ttl
answersFile=$setupFolder/answers/
confFile=$setupFolder/confFileWatDiv
hdtFile=$setupFolder/federationData.hdt
endpointsDescription=$setupFolder/endpointsDescription
updatesFile=$setupFolder/updatesFile

./runAllClients.sh "${strategy}" ${numClients} ${queries} ${numQueries} ${firstPort} ${ldfServer} ${federationFile} ${availability} ${answersFile} ${publicEndpoint} "${engines}" ${confFile} ${hdtFile} ${endpointsDescription} ${updatesFile} ${firstProxyPort} "${sourceSelectionStrategy}" $action "$queriesToExecute" $pePort $peProxyPort $setupFolder
