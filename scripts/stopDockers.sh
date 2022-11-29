#!/bin/bash

#federations="diseasome swdf watDiv linkedMDB geoCoordinates watDiv100"
#suffix=3030
federation=$1

for i in `seq 0 9`; do
  #j=$(($suffix+$i))
  #name=endpoint${j}${federation}
  name=${federation}Endpoint${i}
  docker stop ${name}
done
sleep 10
