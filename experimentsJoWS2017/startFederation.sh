#!/bin/bash
f=$1
folder=${lilachome}/data/${f}Setup
tmpFile=`mktemp`

p=`pwd`

cd ${lilachome}/scripts

for i in `seq 0 9`; do
    port=$(($i+8890))
    address=`./getHost.sh $folder/hosts $port`
    host=http://$address
    oarsh $address "${lilachome}/scripts/startOneEndpoint.sh $f $i $host > ${tmpFile}_$i"
done

sleep 5m

for i in `seq 0 9`; do
   cat ${tmpFile}_$i >> $tmpFile
done

cat $tmpFile

cd $p
