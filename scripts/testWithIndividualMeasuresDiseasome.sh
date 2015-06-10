#!/bin/bash

strategies="FEDERATION"
numClients=10
queriesFile=${fedrahome}/data/diseasomeSetup/queriesB
numQueries=100
firstPort=3030
answersFile=${fedrahome}/data/diseasomeSetup/answers
engines="FedX11 ANAPSID11"
firstProxyPort=3130
sourceSelectionStrategy="Fedra"
setupFolder=${fedrahome}/data/diseasomeSetup
hdtFile=$setupFolder/federationData.hdt
federation=Diseasome
queriesToExecute=$setupFolder/queriesToExecute

./testWithIndividualMeasures.sh "${strategies}" ${numClients} ${queriesFile} ${numQueries} ${firstPort} ${answersFile} "${engines}" ${hdtFile} ${firstProxyPort} "${sourceSelectionStrategy}" $setupFolder $federation "${queriesToExecute}"
