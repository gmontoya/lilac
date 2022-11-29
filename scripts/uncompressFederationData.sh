#!/bin/bash

federations="watDiv diseasome swdf linkedMDB geoCoordinates watDiv100"
p=`pwd`
cd $hdtJavaPath/hdt-java-cli

for f in $federations; do
    ./bin/hdt2rdf.sh $lilachome/data/${f}Setup/federationData.hdt $lilachome/data/${f}Setup/federationData.nt
    rm $lilachome/data/${f}Setup/federationData.hdt
done

cd ${p}
