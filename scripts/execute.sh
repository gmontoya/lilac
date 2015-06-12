#!/bin/bash

federation=$1
Federation=$2
outputRestore=`mktemp`

cd ${fedrahome}/scripts

#./useVirtuosoEndpoints.sh
./restore.sh $federation
./setHosts.sh $federation

#./restoreFederation.sh 3030 3039 ${federation}Setup > $outputRestore
./startFederation.sh ${federation} > $outputRestore

./testWithIndividualMeasures${Federation}.sh > outputTestWithIndividualMeasures${Federation}

sed -i".bkp" 's/sourceSelectionStrategy="[0-9A-Za-z ]*"/sourceSelectionStrategy="Fedra"/' test${Federation}.sh
sed -i".bkp" 's/engines="[0-9A-Za-z ]*"/engines="FedX11 ANAPSID11"/' test${Federation}.sh
sed -i".bkp" 's/sourceSelectionStrategy="[0-9A-Za-z ]*"/sourceSelectionStrategy="Fedra"/' testWithIndividualMeasures${Federation}.sh
sed -i".bkp" 's/engines="[0-9A-Za-z ]*"/engines="FedX11 ANAPSID11"/' testWithIndividualMeasures${Federation}.sh
./testWithIndividualMeasures${Federation}.sh >> outputTestWithIndividualMeasures${Federation}
./endFederation.sh $outputRestore

