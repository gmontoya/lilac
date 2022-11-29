#!/bin/bash

l="watDiv watDiv100 diseasome swdf geoCoordinates linkedMDB"
for f in $l; do
        files="$lilachome/data/${f}Setup/endpointsDescription $lilachome/data/${f}Setup/federation.ttl $lilachome/data/${f}Setup/dawIndex.ttl $lilachome/data/${f}Setup/fedraFiles/endpoints"
        for file in $files; do
            cp $file ${file}.copy
        done
done
