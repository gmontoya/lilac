#!/bin/bash

federations="watDiv diseasome swdf linkedMDB geoCoordinates watDiv100"
cd ${hdtJavaPath}
for i in `seq 3030 3039`; do
    for f in $federations; do
        ./bin/hdt2rdf.sh ${lilachome}/data/${f}Setup/endpoint${i}.hdt ${lilachome}/data/${f}Setup/endpoint${i}.nt
        rm ${lilachome}/data/${f}Setup/endpoint${i}.hdt
    done
done
