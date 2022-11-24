#!/bin/bash

firstPort=$1
last=$2
federation=$3
folder=${lilachome}/data/${federation}
p=`pwd`

for port in `seq $firstPort $last`; do
     cd ${lilachome}/scripts
     address=`./getHost.sh $folder/hosts $port`
     host=http://$address
     oarsh $address "${lilachome}/scripts/restoreOneEndpoint.sh $port $host $folder"
done

sleep 10s

cd $p

