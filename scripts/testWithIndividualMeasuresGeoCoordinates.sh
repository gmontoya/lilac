#!/bin/bash

strategies="FEDERATION"
numClients=10
queriesFile=${fedrahome}/data/geoCoordinatesSetup/queries
numQueries=100
firstPort=8890
answersFile=${fedrahome}/data/geoCoordinatesSetup/answers
engines="ANAPSID"
setupFolder=${fedrahome}/data/geoCoordinatesSetup
hdtFile=$setupFolder/federationData.hdt
firstProxyPort=3130
sourceSelectionStrategy="FedraQR Fedra DAW engine"
federation=GeoCoordinates
queriesToExecute=${fedrahome}/data/geoCoordinatesSetup/queriesToExecute

./testWithIndividualMeasures.sh "${strategies}" ${numClients} ${queriesFile} ${numQueries} ${firstPort} ${answersFile} "${engines}" ${hdtFile} ${firstProxyPort} "${sourceSelectionStrategy}" $setupFolder $federation "${queriesToExecute}"
