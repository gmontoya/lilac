#!/bin/bash

queryAnswer=$1
groundTruth=$2
tmpFile=`mktemp`

cd ${fedrahome}/scripts
python formatANAPSIDoutput.py $queryAnswer > $tmpFile
mv $tmpFile $queryAnswer

./computeCompletenessSoundness.sh $queryAnswer $groundTruth
