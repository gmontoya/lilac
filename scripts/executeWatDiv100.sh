#!/bin/bash

federation=watDiv100
Federation=WatDiv100
outputRestore=`mktemp`

cd ${fedrahome}/scripts
tmpFile=`mktemp`
uniq $OAR_NODEFILE > $tmpFile

tail -n +1 $tmpFile | head -n 11 > ${fedrahome}/data/${federation}Setup/hosts
address=`./getHost.sh ${fedrahome}/data/${federation}Setup/hosts 3040`

cd ${fedrahome}/scripts

./restore.sh $federation
./setHosts.sh $federation

./restoreFederation.sh 3030 3039 ${federation}Setup > $outputRestore

oarsh $address "${fedrahome}/scripts/testWithIndividualMeasures${Federation}.sh"
sed -i".bkp" 's,queriesToExecute="[/0-9A-Za-z ]*",queriesToExecute=${fedrahome}/data/watDiv100Setup/queriesToExecute,' test${Federation}.sh
sed -i".bkp" 's/sourceSelectionStrategy="[0-9A-Za-z ]*"/sourceSelectionStrategy="Fedra"/' test${Federation}.sh
sed -i".bkp" 's/engines="[0-9A-Za-z ]*"/engines="FedX11 ANAPSID11"/' test${Federation}.sh
sed -i".bkp" 's,queriesToExecute="[/0-9A-Za-z ]*",queriesToExecute=${fedrahome}/data/watDiv100Setup/queriesToExecute,' testWithIndividualMeasures${Federation}.sh
sed -i".bkp" 's/sourceSelectionStrategy="[0-9A-Za-z ]*"/sourceSelectionStrategy="Fedra"/' testWithIndividualMeasures${Federation}.sh
sed -i".bkp" 's/engines="[0-9A-Za-z ]*"/engines="FedX11 ANAPSID11"/' testWithIndividualMeasures${Federation}.sh
oarsh $address "${fedrahome}/scripts/testWithIndividualMeasures${Federation}.sh"

./endFederation.sh $outputRestore
