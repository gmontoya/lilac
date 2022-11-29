#!/bin/bash

queryAnswer=$1
groundTruth=$2
tmpFile=`mktemp`

cd $lilachome/scripts
if [ ! -f $queryAnswer ]; then
  touch $queryAnswer
fi
python formatANAPSIDoutput.py $queryAnswer > $tmpFile
mv $tmpFile $queryAnswer

./computeCompletenessSoundness.sh $queryAnswer $groundTruth
