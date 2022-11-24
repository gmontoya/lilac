#!/bin/bash

strategies="FEDERATION"
numClients=10
queriesFile=${lilachome}/data/swdfSetup/queriesC
numQueries=100
firstPort=8890
answersFile=${lilachome}/data/swdfSetup/answers
engines="ANAPSID"
setupFolder=${lilachome}/data/swdfSetup
hdtFile=$setupFolder/federationData.hdt
firstProxyPort=3130
sourceSelectionStrategy="LILAC Fedra DAW engine"
federation=SWDF
queriesToExecute=${lilachome}/data/swdfSetup/queriesToExecute

./testWithIndividualMeasures.sh "${strategies}" ${numClients} ${queriesFile} ${numQueries} ${firstPort} ${answersFile} "${engines}" ${hdtFile} ${firstProxyPort} "${sourceSelectionStrategy}" $setupFolder $federation "${queriesToExecute}"
