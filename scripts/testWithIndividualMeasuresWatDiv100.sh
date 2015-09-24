#!/bin/bash

strategies="FEDERATION"
numClients=10
queriesFile=${fedrahome}/data/watDiv100Setup/queries100_15_500_100_b
numQueries=100
firstPort=8890
answersFile=${fedrahome}/data/watDiv100Setup/answers
engines="ANAPSID"
setupFolder=${fedrahome}/data/watDiv100Setup
hdtFile=$setupFolder/federationData.hdt
firstProxyPort=3130
sourceSelectionStrategy="FedraQR Fedra"
federation=WatDiv100
queriesToExecute=${fedrahome}/data/watDiv100Setup/queriesToExecute
#$setupFolder/queriesToExecute

./testWithIndividualMeasures.sh "${strategies}" ${numClients} ${queriesFile} ${numQueries} ${firstPort} ${answersFile} "${engines}" ${hdtFile} ${firstProxyPort} "${sourceSelectionStrategy}" $setupFolder $federation "${queriesToExecute}"
