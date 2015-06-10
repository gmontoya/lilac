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
queriesToExecute=${fedrahome}/data/geoCoordinatesSetup/queriesToExecute
pePort=9003
peProxyPort=3100
setupFolder=${fedrahome}/data/geoCoordinatesSetup
host=`./getHost.sh $setupFolder/hosts 3040`
host=http://$host
publicEndpoint=${host}:${peProxyPort}/sparql
queries=$setupFolder/queries
firstPort=3030
federation=geo
ldfServer=${host}:5000/${federation}
federationFile=$setupFolder/federation.ttl
answersFile=$setupFolder/answers/
confFile=$setupFolder/confFileGeocoordinates
hdtFile=$setupFolder/federationData.hdt
endpointsDescription=$setupFolder/endpointsDescription
updatesFile=$setupFolder/updatesFile

./runAllClients.sh "${strategy}" ${numClients} ${queries} ${numQueries} ${firstPort} ${ldfServer} ${federationFile} ${availability} ${answersFile} ${publicEndpoint} "${engines}" ${confFile} ${hdtFile} ${endpointsDescription} ${updatesFile} ${firstProxyPort} "${sourceSelectionStrategy}" $action "$queriesToExecute" $pePort $peProxyPort $setupFolder
