#!/bin/bash


source setVariables.sh

cd $fedrahome/proxy
javac  -cp .:$httpcomponentsClientPath/lib/* SingleEndpointProxy2.java

cd $fedrahome/code
javac -cp .:$jenaPath/lib/* *.java

cd $fedrahome/scripts

l="watDiv watDiv100 diseasome swdf geoCoordinates linkedMDB"

for f in $l; do
    sed -i "s,HOST,${host},g" $fedrahome/data/${f}Setup/endpointsDescription
    sed -i "s,HOST,${host},g" $fedrahome/data/${f}Setup/federation.ttl
    sed -i "s,HOST,${host},g" $fedrahome/data/${f}Setup/dawIndex.ttl
    sed -i "s,HOST,${host},g" $fedrahome/data/${f}Setup/fedraFiles/endpoints
done

./restoreFederation.sh 3030 3039 watDivSetup
./testWithIndividualMeasuresWatDiv.sh > outputTestWithIndividualMeasuresWatDiv
killall java

./restoreFederation.sh 3030 3039 diseasomeSetup
./testWithIndividualMeasuresDiseasome.sh > outputTestWithIndividualMeasuresDiseasome
killall java

./restoreFederation.sh 3030 3039 swdfSetup
./testWithIndividualMeasuresSWDF.sh > outputTestWithIndividualMeasuresSWDF
killall java

./restoreFederation.sh 3030 3039 linkedMDBSetup
./testWithIndividualMeasuresLinkedMDB.sh > outputTestWithIndividualMeasuresLinkedMDB
killall java

./restoreFederation.sh 3030 3039 geoCoordinatesSetup
./testWithIndividualMeasuresGeoCoordinates.sh > outputTestWithIndividualMeasuresGeoCoordinates
killall java

./restoreFederation.sh 3030 3039 watDiv100Setup
./testWithIndividualMeasuresWatDiv100.sh > outputTestWithIndividualMeasuresWatDiv100
killall java 

