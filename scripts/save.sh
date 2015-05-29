#!/bin/bash

l="watDiv watDiv100 diseasome swdf geoCoordinates linkedMDB"
for f in $l; do
        files="$fedrahome/data/${f}Setup/endpointsDescription $fedrahome/data/${f}Setup/federation.ttl $fedrahome/data/${f}Setup/dawIndex.ttl $fedrahome/data/${f}Setup/fedraFiles/endpoints"
        for file in $files; do
            cp $file ${file}.copy
        done
done
