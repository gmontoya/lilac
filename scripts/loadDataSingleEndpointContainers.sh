#!/bin/bash


federations="imdb"
#"diseasome swdf watDiv linkedMDB geoCoordinates watDiv100"

for federation in ${federations}; do
  ./restartSingleEndpointContainer.sh $federation
  sleep 60
  ./loadDataSingleEndpointContainerAux.sh $federation
  sleep 60
  ./stopSingleEndpointContainer.sh $federation
done

