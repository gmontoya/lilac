#!/bin/bash

strategies="FEDERATION"
numClients=10
queriesFile=${lilachome}/data/watDiv100Setup/queries100_15_500_100_b
numQueries=100
firstPort=8890
answersFile=${lilachome}/data/watDiv100Setup/answers
engines="ANAPSID"
setupFolder=${lilachome}/data/watDiv100Setup
hdtFile=$setupFolder/federationData.hdt
firstProxyPort=3130
sourceSelectionStrategy="LILAC Fedra"
federation=WatDiv100
queriesToExecute=${lilachome}/data/watDiv100Setup/queriesToExecute
#$setupFolder/queriesToExecute

./testWithIndividualMeasures.sh "${strategies}" ${numClients} ${queriesFile} ${numQueries} ${firstPort} ${answersFile} "${engines}" ${hdtFile} ${firstProxyPort} "${sourceSelectionStrategy}" $setupFolder $federation "${queriesToExecute}"
