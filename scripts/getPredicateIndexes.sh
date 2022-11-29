#!/bin/bash

federations="diseasome swdf watDiv linkedMDB geoCoordinates watDiv100"

cd $lilachome/code

for f in $federations; do
    java -cp .:$jenaPath/lib/* generatePredicateIndex $lilachome/data/${f}Setup/fedraFiles/fragments $lilachome/data/${f}Setup/fedraFiles/predicateIndex
done

