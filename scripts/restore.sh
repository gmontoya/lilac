#!/bin/bash

f=$1

files="${lilachome}/data/${f}Setup/endpointsDescription ${lilachome}/data/${f}Setup/federation.ttl ${lilachome}/data/${f}Setup/dawIndex.ttl ${lilachome}/data/${f}Setup/fedraFiles/endpoints"
for file in $files; do
    cp ${file}.copy ${file}
done
