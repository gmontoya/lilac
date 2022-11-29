#!/bin/bash

numClients=10
numQueries=100
availability=0
strategy="FEDERATION" 
firstProxyPort=3130
engines="ANAPSID"
sourceSelectionStrategy="LILAC Fedra"
action=justExecute
# justSelect justExecute justReplicate all
queriesToExecute=${lilachome}/data/watDiv100Setup/queriesToExecute
#${lilachome}/data/watDiv100Setup/queriesToExecute
pePort=8900
peProxyPort=3100
setupFolder=${lilachome}/data/watDiv100Setup
host=`${lilachome}/experimentsJoWS2017/getHost.sh $setupFolder/hosts 3040`
host=http://$host
if [ "$action" = "justSelect" ]; then
  publicEndpoint=${host}:${pePort}/sparql
else
  publicEndpoint=${host}:${peProxyPort}/sparql
fi
queries=$setupFolder/queries100_15_500_100_b
firstPort=8890
federation=watDiv100
ldfServer=${host}:5000/watDiv100
federationFile=$setupFolder/federation.ttl
answersFile=$setupFolder/answers
confFile=$setupFolder/confFile
hdtFile=$setupFolder/federationData.hdt
endpointsDescription=$setupFolder/endpointsDescription
updatesFile=$setupFolder/updatesFile

./runAllClients.sh "${strategy}" ${numClients} ${queries} ${numQueries} ${firstPort} ${ldfServer} ${federationFile} ${availability} ${answersFile} ${publicEndpoint} "${engines}" ${confFile} ${hdtFile} ${endpointsDescription} ${updatesFile} ${firstProxyPort} "${sourceSelectionStrategy}" $action "$queriesToExecute" $pePort $peProxyPort $setupFolder ${federation}

