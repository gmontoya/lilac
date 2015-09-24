#!/bin/bash

strategies="FEDERATION"
numClients=10
queriesFile=${fedrahome}/data/linkedMDBSetup/queries
numQueries=100
firstPort=8890
answersFile=${fedrahome}/data/linkedMDBSetup/answers
engines="ANAPSID"
setupFolder=${fedrahome}/data/linkedMDBSetup
hdtFile=$setupFolder/federationData.hdt
firstProxyPort=3130
sourceSelectionStrategy="FedraQR Fedra DAW engine"
federation=LinkedMDB
queriesToExecute=${fedrahome}/data/linkedMDBSetup/queriesToExecute
#$setupFolder/queriesToExecute

./testWithIndividualMeasures.sh "${strategies}" ${numClients} ${queriesFile} ${numQueries} ${firstPort} ${answersFile} "${engines}" ${hdtFile} ${firstProxyPort} "${sourceSelectionStrategy}" $setupFolder $federation "${queriesToExecute}"
