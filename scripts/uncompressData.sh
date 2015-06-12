#!/bin/bash

federations="watDiv diseasome swdf linkedMDB geoCoordinates watDiv100"
cd ${hdtJavaPath}
for i in `seq 3030 3039`; do
    for f in $federations; do
        ./hdt2rdf.sh ${fedrahome}/data/${f}Setup/endpoint${i}.hdt ${fedrahome}/data/${f}Setup/endpoint${i}.nt
        rm ${fedrahome}/data/${f}Setup/endpoint${i}.hdt
    done
done
