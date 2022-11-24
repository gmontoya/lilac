#!/bin/bash

federation=$1
Federation=$2
outputRestore=`mktemp`

cd ${lilachome}/scripts

#./useVirtuosoEndpoints.sh
./restore.sh $federation
./setHosts.sh $federation 3130 3100

#./restoreFederation.sh 3030 3039 ${federation}Setup > $outputRestore
cd ${lilachome}/experimentsJoWS2017
./startFederation.sh ${federation} > $outputRestore
sed -i".bkp" 's/action=[0-9A-Za-z ]*/action=justExecute/' test${Federation}.sh
./testWithIndividualMeasures${Federation}.sh > outputTestWithIndividualMeasures${Federation}

sed -i".bkp" 's/action=[0-9A-Za-z ]*/action=justSelect/' test${Federation}.sh
#sed -i".bkp" 's/action=[0-9A-Za-z ]*/action=justSelect/' testWithIndividualMeasures${Federation}.sh

cd ${lilachome}/scripts
./restore.sh $federation
./setHosts.sh $federation 8890 8900

cd ${lilachome}/experimentsJoWS2017
./testWithIndividualMeasures${Federation}.sh >> outputTestWithIndividualMeasures${Federation}
./endFederation.sh $outputRestore

