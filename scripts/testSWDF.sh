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
queriesToExecute=$fedrahome/data/swdfSetup/queriesToExecute
pePort=8996
peProxyPort=3100
host=`./getHost.sh 3040`
host=http://$host
publicEndpoint=${host}:${peProxyPort}/sparql
setupFolder=$fedrahome/data/swdfSetup
queries=$setupFolder/queriesC
firstPort=3030
federation=swdf
ldfServer=${host}:5000/${federation}
federationFile=$setupFolder/federation.ttl
answersFile=$setupFolder/answers/
confFile=$setupFolder/confFileSWDF
hdtFile=$setupFolder/federationData.hdt
endpointsDescription=$setupFolder/endpointsDescription
updatesFile=$setupFolder/updatesFile

./runAllClients.sh "${strategy}" ${numClients} ${queries} ${numQueries} ${firstPort} ${ldfServer} ${federationFile} ${availability} ${answersFile} ${publicEndpoint} "${engines}" ${confFile} ${hdtFile} ${endpointsDescription} ${updatesFile} ${firstProxyPort} "${sourceSelectionStrategy}" $action "$queriesToExecute" $pePort $peProxyPort $setupFolder
