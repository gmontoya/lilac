#!/bin/bash

numClients=10
numQueries=100
availability=0
strategy="FEDERATION" #"FEDERATION LDF PUBLIC"
firstProxyPort=3130
engines="ANAPSID FedX"
sourceSelectionStrategy="Fedra DAW engine" 
action=justExecute
# justExecute justReplicate all
queriesToExecute=$fedrahome/data/diseasomeSetup/queriesToExecute
pePort=8994
peProxyPort=3100
host=`./getHost.sh 3040`
host=http://$host
publicEndpoint=${host}:${peProxyPort}/sparql
setupFolder=$fedrahome/data/diseasomeSetup
queries=$setupFolder/queriesB
firstPort=3030
federation=diseasome
ldfServer=${host}:5000/${federation}
federationFile=$setupFolder/federation.ttl
answersFile=$setupFolder/answers/
confFile=$setupFolder/confFileDiseasome
hdtFile=$setupFolder/federationData.hdt
endpointsDescription=$setupFolder/endpointsDescription
updatesFile=$setupFolder/updatesFile

./runAllClients.sh "${strategy}" ${numClients} ${queries} ${numQueries} ${firstPort} ${ldfServer} ${federationFile} ${availability} ${answersFile} ${publicEndpoint} "${engines}" ${confFile} ${hdtFile} ${endpointsDescription} ${updatesFile} ${firstProxyPort} "${sourceSelectionStrategy}" $action "$queriesToExecute" $pePort $peProxyPort $setupFolder
