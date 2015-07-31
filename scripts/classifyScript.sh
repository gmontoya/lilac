#!/bin/bash

cd /home/montoya/fedra/scripts

./classifyQueries.sh /home/montoya/watDivSetup/queries15_500_100_b

./classifyQueries.sh /home/montoya/diseasomeSetup/queriesB

./classifyQueries.sh /home/montoya/swdfSetup/queriesC

./classifyQueries.sh /home/montoya/geoCoordinatesSetup/queries

./classifyQueries.sh /home/montoya/linkedMDBSetup/queries

./classifyQueries.sh /home/montoya/watDiv100Setup/queries100_15_500_100_b
