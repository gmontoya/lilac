#!/bin/bash

numClients=10
numQueries=100
availability=0
strategy="FEDERATION" 
firstProxyPort=3130
engines="FedX11 ANAPSID11"
sourceSelectionStrategy="Fedra" 
action=justExecute 
# justExecute justReplicate all
queriesToExecute=${fedrahome}/data/watDivSetup/queriesToExecute
pePort=8890
peProxyPort=3100
setupFolder=${fedrahome}/data/watDivSetup
host=`./getHost.sh $setupFolder/hosts 3040`
host=http://$host
publicEndpoint=${host}:${peProxyPort}/sparql
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
