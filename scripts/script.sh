#!/bin/bash


source setVariables.sh

cd $fedrahome/proxy
javac  -cp .:$httpcomponentsClientPath/lib/* SingleEndpointProxy2.java

cd $fedrahome/code
javac -cp .:$jenaPath/lib/* *.java

cd $fedrahome/scripts

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

./restoreFederation.sh 3030 3039 watDivSetup
./testWithIndividualMeasuresWatDiv.sh > outputTestWithIndividualMeasuresWatDiv
sed -i".bkp" 's/sourceSelectionStrategy="[0-9A-Za-z ]*"/sourceSelectionStrategy="Fedra"/' testWatDiv.sh
sed -i".bkp" 's/engines="[0-9A-Za-z ]*"/engines="FedX11 ANAPSID11"/' testWatDiv.sh
sed -i".bkp" 's/sourceSelectionStrategy="[0-9A-Za-z ]*"/sourceSelectionStrategy="Fedra"/' testWithIndividualMeasuresWatDiv.sh
sed -i".bkp" 's/engines="[0-9A-Za-z ]*"/engines="FedX11 ANAPSID11"/' testWithIndividualMeasuresWatDiv.sh
./testWithIndividualMeasuresWatDiv.sh >> outputTestWithIndividualMeasuresWatDiv
killall java

./restoreFederation.sh 3030 3039 diseasomeSetup
./testWithIndividualMeasuresDiseasome.sh > outputTestWithIndividualMeasuresDiseasome
sed -i".bkp" 's/sourceSelectionStrategy="[0-9A-Za-z ]*"/sourceSelectionStrategy="Fedra"/' testDiseasome.sh
sed -i".bkp" 's/engines="[0-9A-Za-z ]*"/engines="FedX11 ANAPSID11"/' testDiseasome.sh
sed -i".bkp" 's/sourceSelectionStrategy="[0-9A-Za-z ]*"/sourceSelectionStrategy="Fedra"/' testWithIndividualMeasuresDiseasome.sh
sed -i".bkp" 's/engines="[0-9A-Za-z ]*"/engines="FedX11 ANAPSID11"/' testWithIndividualMeasuresDiseasome.sh
./testWithIndividualMeasuresDiseasome.sh >> outputTestWithIndividualMeasuresDiseasome
killall java

./restoreFederation.sh 3030 3039 swdfSetup
./testWithIndividualMeasuresSWDF.sh > outputTestWithIndividualMeasuresSWDF
sed -i".bkp" 's/sourceSelectionStrategy="[0-9A-Za-z ]*"/sourceSelectionStrategy="Fedra"/' testSWDF.sh
sed -i".bkp" 's/engines="[0-9A-Za-z ]*"/engines="FedX11 ANAPSID11"/' testSWDF.sh
sed -i".bkp" 's/sourceSelectionStrategy="[0-9A-Za-z ]*"/sourceSelectionStrategy="Fedra"/' testWithIndividualMeasuresSWDF.sh
sed -i".bkp" 's/engines="[0-9A-Za-z ]*"/engines="FedX11 ANAPSID11"/' testWithIndividualMeasuresSWDF.sh
./testWithIndividualMeasuresSWDF.sh >> outputTestWithIndividualMeasuresSWDF
killall java

./restoreFederation.sh 3030 3039 linkedMDBSetup
./testWithIndividualMeasuresLinkedMDB.sh > outputTestWithIndividualMeasuresLinkedMDB
sed -i".bkp" 's/sourceSelectionStrategy="[0-9A-Za-z ]*"/sourceSelectionStrategy="Fedra"/' testLinkedMDB.sh
sed -i".bkp" 's/engines="[0-9A-Za-z ]*"/engines="FedX11 ANAPSID11"/' testLinkedMDB.sh
sed -i".bkp" 's/sourceSelectionStrategy="[0-9A-Za-z ]*"/sourceSelectionStrategy="Fedra"/' testWithIndividualMeasuresLinkedMDB.sh
sed -i".bkp" 's/engines="[0-9A-Za-z ]*"/engines="FedX11 ANAPSID11"/' testWithIndividualMeasuresLinkedMDB.sh
./testWithIndividualMeasuresLinkedMDB.sh >> outputTestWithIndividualMeasuresLinkedMDB
killall java

./restoreFederation.sh 3030 3039 geoCoordinatesSetup
./testWithIndividualMeasuresGeoCoordinates.sh > outputTestWithIndividualMeasuresGeoCoordinates
sed -i".bkp" 's/sourceSelectionStrategy="[0-9A-Za-z ]*"/sourceSelectionStrategy="Fedra"/' testGeoCoordinates.sh
sed -i".bkp" 's/engines="[0-9A-Za-z ]*"/engines="FedX11 ANAPSID11"/' testGeoCoordinates.sh
sed -i".bkp" 's/sourceSelectionStrategy="[0-9A-Za-z ]*"/sourceSelectionStrategy="Fedra"/' testWithIndividualMeasuresGeoCoordinates.sh
sed -i".bkp" 's/engines="[0-9A-Za-z ]*"/engines="FedX11 ANAPSID11"/' testWithIndividualMeasuresGeoCoordinates.sh
./testWithIndividualMeasuresGeoCoordinates.sh >> outputTestWithIndividualMeasuresGeoCoordinates
killall java

./restoreFederation.sh 3030 3039 watDiv100Setup
./testWithIndividualMeasuresWatDiv100.sh > outputTestWithIndividualMeasuresWatDiv100
sed -i".bkp" 's/sourceSelectionStrategy="[0-9A-Za-z ]*"/sourceSelectionStrategy="Fedra"/' testWatDiv100.sh
sed -i".bkp" 's/engines="[0-9A-Za-z ]*"/engines="FedX11 ANAPSID11"/' testWatDiv100.sh
sed -i".bkp" 's/sourceSelectionStrategy="[0-9A-Za-z ]*"/sourceSelectionStrategy="Fedra"/' testWithIndividualMeasuresWatDiv100.sh
sed -i".bkp" 's/engines="[0-9A-Za-z ]*"/engines="FedX11 ANAPSID11"/' testWithIndividualMeasuresWatDiv100.sh
./testWithIndividualMeasuresWatDiv100.sh >> outputTestWithIndividualMeasuresWatDiv100
killall java 

