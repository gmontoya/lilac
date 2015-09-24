#!/bin/bash

strategies="FEDERATION"
numClients=10
queriesFile=${fedrahome}/data/swdfSetup/queriesC
numQueries=100
firstPort=8890
answersFile=${fedrahome}/data/swdfSetup/answers
engines="ANAPSID"
setupFolder=${fedrahome}/data/swdfSetup
hdtFile=$setupFolder/federationData.hdt
firstProxyPort=3130
sourceSelectionStrategy="FedraQR Fedra DAW engine"
federation=SWDF
queriesToExecute=${fedrahome}/data/swdfSetup/queriesToExecute

./testWithIndividualMeasures.sh "${strategies}" ${numClients} ${queriesFile} ${numQueries} ${firstPort} ${answersFile} "${engines}" ${hdtFile} ${firstProxyPort} "${sourceSelectionStrategy}" $setupFolder $federation "${queriesToExecute}"
