#!/bin/bash

strategies="FEDERATION"
numClients=10
queriesFile=${fedrahome}/data/geoCoordinatesSetup/queries
numQueries=100
firstPort=3030
answersFile=${fedrahome}/data/geoCoordinatesSetup/answers
engines="FedX11 ANAPSID11"
setupFolder=${fedrahome}/data/geoCoordinatesSetup
hdtFile=$setupFolder/federationData.hdt
firstProxyPort=3130
sourceSelectionStrategy="Fedra"
federation=GeoCoordinates
queriesToExecute=$setupFolder/queriesToExecute

./testWithIndividualMeasures.sh "${strategies}" ${numClients} ${queriesFile} ${numQueries} ${firstPort} ${answersFile} "${engines}" ${hdtFile} ${firstProxyPort} "${sourceSelectionStrategy}" $setupFolder $federation "${queriesToExecute}"
