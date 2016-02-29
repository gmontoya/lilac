#!/bin/bash

strategies="FEDERATION"
numClients=10
queriesFile=${lilachome}/data/watDivSetup/queries15_500_100_b
numQueries=100
firstPort=8890
answersFile=${lilachome}/data/watDivSetup/answers
engines="ANAPSID"
firstProxyPort=3130
sourceSelectionStrategy="LILAC Fedra DAW engine"
setupFolder=${lilachome}/data/watDivSetup
hdtFile=$setupFolder/federationData.hdt
federation=WatDiv
queriesToExecute=${lilachome}/data/watDivSetup/queriesToExecute
#$setupFolder/queriesToExecute

./testWithIndividualMeasures.sh "${strategies}" ${numClients} ${queriesFile} ${numQueries} ${firstPort} ${answersFile} "${engines}" ${hdtFile} ${firstProxyPort} "${sourceSelectionStrategy}" $setupFolder $federation "${queriesToExecute}"
