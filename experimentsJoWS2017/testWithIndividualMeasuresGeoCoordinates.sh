#!/bin/bash

strategies="FEDERATION"
numClients=10
queriesFile=${lilachome}/data/geoCoordinatesSetup/queries
numQueries=100
firstPort=8890
answersFile=${lilachome}/data/geoCoordinatesSetup/answers
engines="ANAPSID"
setupFolder=${lilachome}/data/geoCoordinatesSetup
hdtFile=$setupFolder/federationData.hdt
firstProxyPort=3130
sourceSelectionStrategy="LILAC Fedra DAW engine"
federation=GeoCoordinates
queriesToExecute=${lilachome}/data/geoCoordinatesSetup/queriesToExecute

./testWithIndividualMeasures.sh "${strategies}" ${numClients} ${queriesFile} ${numQueries} ${firstPort} ${answersFile} "${engines}" ${hdtFile} ${firstProxyPort} "${sourceSelectionStrategy}" $setupFolder $federation "${queriesToExecute}"
