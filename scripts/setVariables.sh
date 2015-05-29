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
    sed -i "s,\${$v},${!v},g" *.sh
    sed -i "s,\$$v,${!v},g" *.sh
    sed -i "s,\$$v,${!v},g" $fedrahome/data/diseasomeSetup/confFileDiseasome
    sed -i "s,\$$v,${!v},g" $fedrahome/data/swdfSetup/confFileSWDF
    sed -i "s,\$$v,${!v},g" $fedrahome/data/linkedMDBSetup/confFileLinkedMDB
    sed -i "s,\$$v,${!v},g" $fedrahome/data/geoCoordinatesSetup/confFileGeocoordinates
    sed -i "s,\$$v,${!v},g" $fedrahome/data/watDivSetup/confFileWatDiv
    sed -i "s,\$$v,${!v},g" $fedrahome/data/watDiv100Setup/confFileWatDiv100
done

l="watDiv watDiv100 diseasome swdf geoCoordinates linkedMDB"
ports="3100 3130 3131 3132 3133 3134 3135 3136 3137 3138 3139"
for port in $ports; do
    p=$(($port-100))
    address=`./getHost.sh ${p}`
    host=http://$address
    for f in $l; do
        files="$fedrahome/data/${f}Setup/endpointsDescription $fedrahome/data/${f}Setup/federation.ttl $fedrahome/data/${f}Setup/dawIndex.ttl $fedrahome/data/${f}Setup/fedraFiles/endpoints"
        for file in $files; do
            sed -i "s,HOST:$port,${host}:$port,g" $file
        done
    done
done

rm *.bkp
