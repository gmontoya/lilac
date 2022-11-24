#!/bin/bash

strategies="FEDERATION"
numClients=10
queriesFile=${lilachome}/data/linkedMDBSetup/queries
numQueries=100
firstPort=8890
answersFile=${lilachome}/data/linkedMDBSetup/answers
engines="ANAPSID"
setupFolder=${lilachome}/data/linkedMDBSetup
hdtFile=$setupFolder/federationData.hdt
firstProxyPort=3130
sourceSelectionStrategy="LILAC Fedra DAW engine"
federation=LinkedMDB
queriesToExecute=${lilachome}/data/linkedMDBSetup/queriesToExecute
#$setupFolder/queriesToExecute

./testWithIndividualMeasures.sh "${strategies}" ${numClients} ${queriesFile} ${numQueries} ${firstPort} ${answersFile} "${engines}" ${hdtFile} ${firstProxyPort} "${sourceSelectionStrategy}" $setupFolder $federation "${queriesToExecute}"
