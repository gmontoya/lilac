#!/bin/bash

strategies="FEDERATION"
numClients=10
queriesFile=$fedrahome/data/swdfSetup/queriesC
numQueries=100
firstPort=3030
answersFile=$fedrahome/data/swdfSetup/answers
engines="ANAPSID FedX"
setupFolder=$fedrahome/data/swdfSetup
hdtFile=$setupFolder/federationData.hdt
firstProxyPort=3130
sourceSelectionStrategy="Fedra DAW engine"
federation=SWDF
queriesToExecute=$setupFolder/queriesToExecute

./testWithIndividualMeasures.sh "${strategies}" ${numClients} ${queriesFile} ${numQueries} ${firstPort} ${answersFile} "${engines}" ${hdtFile} ${firstProxyPort} "${sourceSelectionStrategy}" $setupFolder $federation "${queriesToExecute}"
