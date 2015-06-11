#!/bin/bash

mv restoreOneEndpoint.sh restoreOneEndpoint.sh.fuseki
mv restoreOneEndpoint.sh.virtuoso restoreOneEndpoint.sh

./execute0.sh watDiv WatDiv

./execute0.sh diseasome Diseasome

./execute0.sh swdf SWDF

./execute0.sh linkedMDB LinkedMDB

./execute0.sh geoCoordinates GeoCoordinates

./execute0.sh watDiv100 WatDiv100

