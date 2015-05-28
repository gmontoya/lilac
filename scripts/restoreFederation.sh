#!/bin/bash

firstPort=$1
last=$2
federation=$3
folder=$fedrahome/data/${federation}
p=`pwd`

for port in `seq $firstPort $last`; do
     cd $fedrahome/scripts
     address=`./getHost.sh $port`
     host=http://$address
     oarsh $address "./restoreOneEndpoint.sh $port $host $folder"
done


cd $p

