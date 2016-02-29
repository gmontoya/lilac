#!/bin/bash

federation=$1
Federation=$2
outputRestore=`mktemp`

cd ${lilachome}/scripts

#./useVirtuosoEndpoints.sh
./restore.sh $federation
./setHosts.sh $federation

#./restoreFederation.sh 3030 3039 ${federation}Setup > $outputRestore
./startFederation.sh ${federation} > $outputRestore

./testWithIndividualMeasures${Federation}.sh > outputTestWithIndividualMeasures${Federation}

sed -i".bkp" 's/action=[0-9A-Za-z ]*/action=justSelect/' test${Federation}.sh
sed -i".bkp" 's/action=[0-9A-Za-z ]*/action=justSelect/' testWithIndividualMeasures${Federation}.sh

cd ${lilachome}/scripts
./restore.sh $federation
./setHosts.sh $federation 8890 8900

./testWithIndividualMeasures${Federation}.sh >> outputTestWithIndividualMeasures${Federation}
./removeLinks.sh $federation
./endFederation.sh $outputRestore

