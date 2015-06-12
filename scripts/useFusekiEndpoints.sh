#!/bin/bash

files="endpointsDescription federation.ttl dawIndex.ttl fedraFiles/endpoints"
federations="watDiv diseasome swdf linkedMDB geoCoordinates watDiv100"

for federation in $federations; do
  for f in $files; do
    for j in `seq 3130 3139`; do
      sed -i "s,HOST:$j/sparql,HOST:$j/ds/sparql,g" ${fedrahome}/data/${federation}Setup/${f}.copy
    done
  done
done
