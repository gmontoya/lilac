#!/bin/bash

fedrahome=/home/gmontoya/fedra
fusekiPath=/home/gmontoya/logiciels/jena-fuseki-1.1.1
dawIndexGeneratorPath=/home/gmontoya/logiciels/FedraDawIndex
fusekiHDTPath=/home/gmontoya/logiciels/hdt-fuseki-1.1.1-SNAPSHOT
httpcomponentsClientPath=/home/gmontoya/logiciels/httpcomponents-client-4.3.5
watdivPath=/home/gmontoya/logiciels/watdiv
fedXPath=/home/gmontoya/logiciels/fedX
jenaPath=/home/gmontoya/logiciels/apache-jena-2.11.0
anapsidPath=/home/gmontoya/logiciels/anapsid-master
variables="fedrahome fusekiPath dawIndexGeneratorPath fusekiHDTPath httpcomponentsClientPath watdivPath fedXPath jenaPath anapsidPath"

for v in $variables; do
    sed -i "s,${!v},\${$v},g" /home/gmontoya/fedra/scripts/*.sh
    sed -i "s,${!v},\$$v,g" /home/gmontoya/fedra/scripts/*.sh
    sed -i "s,${!v},\$$v,g" /home/gmontoya/fedra/data/diseasomeSetup/confFileDiseasome
    sed -i "s,${!v},\$$v,g" /home/gmontoya/fedra/data/swdfSetup/confFileSWDF
    sed -i "s,${!v},\$$v,g" /home/gmontoya/fedra/data/linkedMDBSetup/confFileLinkedMDB
    sed -i "s,${!v},\$$v,g" /home/gmontoya/fedra/data/geoCoordinatesSetup/confFileGeocoordinates
    sed -i "s,${!v},\$$v,g" /home/gmontoya/fedra/data/watDivSetup/confFileWatDiv
    sed -i "s,${!v},\$$v,g" /home/gmontoya/fedra/data/watDiv100Setup/confFileWatDiv100
done
