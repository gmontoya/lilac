#!/bin/bash

numClients=10
numQueries=100
availability=0
strategy="FEDERATION"
firstProxyPort=3130
engines="ANAPSID"
sourceSelectionStrategy="LILAC Fedra DAW engine"
action=justSelect
# justSelect justExecute justReplicate all
queriesToExecute=${lilachome}/data/geoCoordinatesSetup/queriesToExecute
pePort=8900
peProxyPort=3100
setupFolder=${lilachome}/data/geoCoordinatesSetup
host=`${lilachome}/experimentsJoWS2017/getHost.sh $setupFolder/hosts 3040`
host=http://$host
if [ "$action" = "justSelect" ]; then
  publicEndpoint=${host}:${pePort}/sparql
else
  publicEndpoint=${host}:${peProxyPort}/sparql
fi
queries=$setupFolder/queries
firstPort=8890
federation=geoCoordinates
ldfServer=${host}:5000/geo
federationFile=$setupFolder/federation.ttl
answersFile=$setupFolder/answersVirtuoso/
confFile=$setupFolder/confFile
hdtFile=$setupFolder/federationData.hdt
endpointsDescription=$setupFolder/endpointsDescription
updatesFile=$setupFolder/updatesFile

./runAllClients.sh "${strategy}" ${numClients} ${queries} ${numQueries} ${firstPort} ${ldfServer} ${federationFile} ${availability} ${answersFile} ${publicEndpoint} "${engines}" ${confFile} ${hdtFile} ${endpointsDescription} ${updatesFile} ${firstProxyPort} "${sourceSelectionStrategy}" $action "$queriesToExecute" $pePort $peProxyPort $setupFolder ${federation}

