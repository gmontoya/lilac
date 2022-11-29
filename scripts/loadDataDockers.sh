#!/bin/bash


federations="diseasome swdf watDiv linkedMDB geoCoordinates watDiv100"

for federation in ${federations}; do
  ./restartDockers.sh $federation
  sleep 60
  ./loadFederationsData.sh $federation
  sleep 60
  ./stopDockers.sh $federation
done

