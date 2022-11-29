#!/bin/bash

#federations="diseasome swdf watDiv linkedMDB geoCoordinates watDiv100"
#suffix=3030
federation=$1


for i in `seq 0 9`; do
  name=${federation}Endpoint${i}
  docker start ${name}
done

sleep 30

p=`pwd`
cd $lilachome/scripts

for i in `seq 0 9`; do
  ./clearOtherGraphs.sh ${federation}
done

cd $p

sleep 30
