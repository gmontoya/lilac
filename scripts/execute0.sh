#!/bin/bash

federation=$1
Federation=$2

cd ${lilachome}/scripts
tmpFile=`mktemp`
uniq $OAR_NODEFILE > $tmpFile

tail -n +1 $tmpFile | head -n 11 > ${lilachome}/data/${federation}Setup/hosts
address=`./getHost.sh ${lilachome}/data/${federation}Setup/hosts 3040`
oarsh $address "${lilachome}/scripts/execute.sh ${federation} ${Federation}"

