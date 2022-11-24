#!/bin/bash

strategies="FEDERATION"
numClients=10
queriesFile=${lilachome}/data/diseasomeSetup/queriesB
numQueries=100
firstPort=8890
answersFile=${lilachome}/data/diseasomeSetup/answers
engines="ANAPSID"
firstProxyPort=3130
sourceSelectionStrategy="LILAC Fedra DAW engine"
setupFolder=${lilachome}/data/diseasomeSetup
hdtFile=$setupFolder/federationData.hdt
federation=Diseasome
queriesToExecute=${lilachome}/data/diseasomeSetup/queriesToExecute

./testWithIndividualMeasures.sh "${strategies}" ${numClients} ${queriesFile} ${numQueries} ${firstPort} ${answersFile} "${engines}" ${hdtFile} ${firstProxyPort} "${sourceSelectionStrategy}" $setupFolder $federation "${queriesToExecute}"
