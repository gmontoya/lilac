#!/bin/bash

f=$1

files="${fedrahome}/data/${f}Setup/endpointsDescription ${fedrahome}/data/${f}Setup/federation.ttl ${fedrahome}/data/${f}Setup/dawIndex.ttl ${fedrahome}/data/${f}Setup/fedraFiles/endpoints"
for file in $files; do
    cp ${file}.copy ${file}
done
