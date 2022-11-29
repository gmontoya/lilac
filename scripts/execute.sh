#!/bin/bash

federation=$1
Federation=$2

setupFolder=$lilachome/data/${federation}Setup
confFile=$setupFolder/confFile

source ${confFile}

cd $lilachome/scripts

./restore.sh $federation
./setHosts.sh $federation ${firstProxyPort} ${peProxyPort}

##./setupTest.sh ${federation} ${Federation}

./restartDockers.sh ${federation}

./testWithIndividualMeasures.sh ${federation} ${Federation} ${queriesToExecute} "justExecute" > outputTestWithIndividualMeasures${Federation}

cd $lilachome/scripts
./restore.sh $federation
./setHosts.sh $federation ${firstPort} ${pePort}

#source ${confFile}

##./testWithIndividualMeasures.sh ${federation} ${Federation} ${queriesToExecute} "justSelect" >> outputTestWithIndividualMeasures${Federation}

./stopDockers.sh ${federation}
