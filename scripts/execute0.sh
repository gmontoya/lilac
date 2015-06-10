#!/bin/bash

federation=$1
Federation=$2

cd ${fedrahome}/scripts
tmpFile=`mktemp`
uniq $OAR_NODEFILE > $tmpFile

tail -n +1 $tmpFile | head -n 11 > ${fedrahome}/data/${federation}Setup/hosts
address=`./getHost.sh ${fedrahome}/data/${federation}Setup/hosts 3040`
oarsh $address "${fedrahome}/scripts/execute.sh ${federation} ${Federation}"

