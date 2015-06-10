#!/bin/bash

strategies="FEDERATION"
numClients=10
queriesFile=${fedrahome}/data/watDivSetup/queries15_500_100_b
numQueries=100
firstPort=3030
answersFile=${fedrahome}/data/watDivSetup/answers
engines="FedX11 ANAPSID11"
firstProxyPort=3130
sourceSelectionStrategy="Fedra"
setupFolder=${fedrahome}/data/watDivSetup
hdtFile=$setupFolder/federationData.hdt
federation=WatDiv
queriesToExecute=$setupFolder/queriesToExecute

./testWithIndividualMeasures.sh "${strategies}" ${numClients} ${queriesFile} ${numQueries} ${firstPort} ${answersFile} "${engines}" ${hdtFile} ${firstProxyPort} "${sourceSelectionStrategy}" $setupFolder $federation "${queriesToExecute}"
