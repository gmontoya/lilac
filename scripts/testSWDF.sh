#!/bin/bash

numClients=10
numQueries=100
availability=0
strategy="FEDERATION"
firstProxyPort=3130
engines="ANAPSID"
sourceSelectionStrategy="FedraQR Fedra DAW engine" 
action=justSelect
# justSelect justExecute justReplicate all
queriesToExecute=${fedrahome}/data/swdfSetup/queriesToExecute
pePort=8900
peProxyPort=3100
setupFolder=${fedrahome}/data/swdfSetup
host=`./getHost.sh $setupFolder/hosts 3040`
host=http://$host
if [ "$action" = "justSelect" ]; then
  publicEndpoint=${host}:${pePort}/sparql
else
  publicEndpoint=${host}:${peProxyPort}/sparql
fi
queries=$setupFolder/queriesC
firstPort=8890
federation=swdf
ldfServer=${host}:5000/${federation}
federationFile=$setupFolder/federation.ttl
answersFile=$setupFolder/answers/
confFile=$setupFolder/confFileSWDF
hdtFile=$setupFolder/federationData.hdt
endpointsDescription=$setupFolder/endpointsDescription
updatesFile=$setupFolder/updatesFile

./runAllClients.sh "${strategy}" ${numClients} ${queries} ${numQueries} ${firstPort} ${ldfServer} ${federationFile} ${availability} ${answersFile} ${publicEndpoint} "${engines}" ${confFile} ${hdtFile} ${endpointsDescription} ${updatesFile} ${firstProxyPort} "${sourceSelectionStrategy}" $action "$queriesToExecute" $pePort $peProxyPort $setupFolder ${federation}
