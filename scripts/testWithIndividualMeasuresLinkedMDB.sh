#!/bin/bash

strategies="FEDERATION"
numClients=10
queriesFile=${fedrahome}/data/linkedMDBSetup/queries
numQueries=100
firstPort=3030
answersFile=${fedrahome}/data/linkedMDBSetup/answers
engines="FedX11 ANAPSID11"
setupFolder=${fedrahome}/data/linkedMDBSetup
hdtFile=$setupFolder/federationData.hdt
firstProxyPort=3130
sourceSelectionStrategy="Fedra"
federation=LinkedMDB
queriesToExecute=$setupFolder/queriesToExecute

./testWithIndividualMeasures.sh "${strategies}" ${numClients} ${queriesFile} ${numQueries} ${firstPort} ${answersFile} "${engines}" ${hdtFile} ${firstProxyPort} "${sourceSelectionStrategy}" $setupFolder $federation "${queriesToExecute}"
