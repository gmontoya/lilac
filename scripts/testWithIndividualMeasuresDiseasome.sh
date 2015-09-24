#!/bin/bash

strategies="FEDERATION"
numClients=10
queriesFile=${fedrahome}/data/diseasomeSetup/queriesB
numQueries=100
firstPort=8890
answersFile=${fedrahome}/data/diseasomeSetup/answers
engines="ANAPSID"
firstProxyPort=3130
sourceSelectionStrategy="FedraQR Fedra DAW engine"
setupFolder=${fedrahome}/data/diseasomeSetup
hdtFile=$setupFolder/federationData.hdt
federation=Diseasome
queriesToExecute=${fedrahome}/data/diseasomeSetup/queriesToExecute

./testWithIndividualMeasures.sh "${strategies}" ${numClients} ${queriesFile} ${numQueries} ${firstPort} ${answersFile} "${engines}" ${hdtFile} ${firstProxyPort} "${sourceSelectionStrategy}" $setupFolder $federation "${queriesToExecute}"
