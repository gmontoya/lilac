#!/bin/bash

cd $fedrahome/scripts

./restoreFederation.sh 3030 3039 watDivSetup > outputRestore
./testWithIndividualMeasuresWatDiv.sh > outputTestWithIndividualMeasuresWatDiv
sed -i".bkp" 's/sourceSelectionStrategy="[0-9A-Za-z ]*"/sourceSelectionStrategy="Fedra"/' testWatDiv.sh
sed -i".bkp" 's/engines="[0-9A-Za-z ]*"/engines="FedX11 ANAPSID11"/' testWatDiv.sh
sed -i".bkp" 's/sourceSelectionStrategy="[0-9A-Za-z ]*"/sourceSelectionStrategy="Fedra"/' testWithIndividualMeasuresWatDiv.sh
sed -i".bkp" 's/engines="[0-9A-Za-z ]*"/engines="FedX11 ANAPSID11"/' testWithIndividualMeasuresWatDiv.sh
./testWithIndividualMeasuresWatDiv.sh >> outputTestWithIndividualMeasuresWatDiv
./endFederation.sh outputRestore

./restoreFederation.sh 3030 3039 diseasomeSetup > outputRestore
./testWithIndividualMeasuresDiseasome.sh > outputTestWithIndividualMeasuresDiseasome
sed -i".bkp" 's/sourceSelectionStrategy="[0-9A-Za-z ]*"/sourceSelectionStrategy="Fedra"/' testDiseasome.sh
sed -i".bkp" 's/engines="[0-9A-Za-z ]*"/engines="FedX11 ANAPSID11"/' testDiseasome.sh
sed -i".bkp" 's/sourceSelectionStrategy="[0-9A-Za-z ]*"/sourceSelectionStrategy="Fedra"/' testWithIndividualMeasuresDiseasome.sh
sed -i".bkp" 's/engines="[0-9A-Za-z ]*"/engines="FedX11 ANAPSID11"/' testWithIndividualMeasuresDiseasome.sh
./testWithIndividualMeasuresDiseasome.sh >> outputTestWithIndividualMeasuresDiseasome
./endFederation.sh outputRestore

./restoreFederation.sh 3030 3039 swdfSetup  > outputRestore
./testWithIndividualMeasuresSWDF.sh > outputTestWithIndividualMeasuresSWDF
sed -i".bkp" 's/sourceSelectionStrategy="[0-9A-Za-z ]*"/sourceSelectionStrategy="Fedra"/' testSWDF.sh
sed -i".bkp" 's/engines="[0-9A-Za-z ]*"/engines="FedX11 ANAPSID11"/' testSWDF.sh
sed -i".bkp" 's/sourceSelectionStrategy="[0-9A-Za-z ]*"/sourceSelectionStrategy="Fedra"/' testWithIndividualMeasuresSWDF.sh
sed -i".bkp" 's/engines="[0-9A-Za-z ]*"/engines="FedX11 ANAPSID11"/' testWithIndividualMeasuresSWDF.sh
./testWithIndividualMeasuresSWDF.sh >> outputTestWithIndividualMeasuresSWDF
./endFederation.sh outputRestore

./restoreFederation.sh 3030 3039 linkedMDBSetup > outputRestore
./testWithIndividualMeasuresLinkedMDB.sh > outputTestWithIndividualMeasuresLinkedMDB
sed -i".bkp" 's/sourceSelectionStrategy="[0-9A-Za-z ]*"/sourceSelectionStrategy="Fedra"/' testLinkedMDB.sh
sed -i".bkp" 's/engines="[0-9A-Za-z ]*"/engines="FedX11 ANAPSID11"/' testLinkedMDB.sh
sed -i".bkp" 's/sourceSelectionStrategy="[0-9A-Za-z ]*"/sourceSelectionStrategy="Fedra"/' testWithIndividualMeasuresLinkedMDB.sh
sed -i".bkp" 's/engines="[0-9A-Za-z ]*"/engines="FedX11 ANAPSID11"/' testWithIndividualMeasuresLinkedMDB.sh
./testWithIndividualMeasuresLinkedMDB.sh >> outputTestWithIndividualMeasuresLinkedMDB
./endFederation.sh outputRestore

./restoreFederation.sh 3030 3039 geoCoordinatesSetup > outputRestore
./testWithIndividualMeasuresGeoCoordinates.sh > outputTestWithIndividualMeasuresGeoCoordinates
sed -i".bkp" 's/sourceSelectionStrategy="[0-9A-Za-z ]*"/sourceSelectionStrategy="Fedra"/' testGeoCoordinates.sh
sed -i".bkp" 's/engines="[0-9A-Za-z ]*"/engines="FedX11 ANAPSID11"/' testGeoCoordinates.sh
sed -i".bkp" 's/sourceSelectionStrategy="[0-9A-Za-z ]*"/sourceSelectionStrategy="Fedra"/' testWithIndividualMeasuresGeoCoordinates.sh
sed -i".bkp" 's/engines="[0-9A-Za-z ]*"/engines="FedX11 ANAPSID11"/' testWithIndividualMeasuresGeoCoordinates.sh
./testWithIndividualMeasuresGeoCoordinates.sh >> outputTestWithIndividualMeasuresGeoCoordinates
./endFederation.sh outputRestore

./restoreFederation.sh 3030 3039 watDiv100Setup > outputRestore
./testWithIndividualMeasuresWatDiv100.sh > outputTestWithIndividualMeasuresWatDiv100
sed -i".bkp" 's/sourceSelectionStrategy="[0-9A-Za-z ]*"/sourceSelectionStrategy="Fedra"/' testWatDiv100.sh
sed -i".bkp" 's/engines="[0-9A-Za-z ]*"/engines="FedX11 ANAPSID11"/' testWatDiv100.sh
sed -i".bkp" 's/sourceSelectionStrategy="[0-9A-Za-z ]*"/sourceSelectionStrategy="Fedra"/' testWithIndividualMeasuresWatDiv100.sh
sed -i".bkp" 's/engines="[0-9A-Za-z ]*"/engines="FedX11 ANAPSID11"/' testWithIndividualMeasuresWatDiv100.sh
./testWithIndividualMeasuresWatDiv100.sh >> outputTestWithIndividualMeasuresWatDiv100
./endFederation.sh outputRestore

