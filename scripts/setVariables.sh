#!/bin/bash

fedrahome=${fedrahome}
fusekiPath=${fusekiPath}
dawIndexGeneratorPath=${dawIndexGeneratorPath}
fusekiHDTPath=${fusekiHDTPath}
httpcomponentsClientPath=${httpcomponentsClientPath}
watdivPath=${watdivPath}
fedXPath=${fedXPath}
jenaPath=${jenaPath}
anapsidPath=${anapsidPath}
variables="fedrahome fusekiPath dawIndexGeneratorPath fusekiHDTPath httpcomponentsClientPath watdivPath fedXPath jenaPath anapsidPath"

for v in $variables; do
    sed -i "s,\${$v},${!v},g" *.sh
    sed -i "s,\$$v,${!v},g" *.sh
    sed -i "s,\$$v,${!v},g" ${fedrahome}/data/diseasomeSetup/confFileDiseasome
    sed -i "s,\$$v,${!v},g" ${fedrahome}/data/swdfSetup/confFileSWDF
    sed -i "s,\$$v,${!v},g" ${fedrahome}/data/linkedMDBSetup/confFileLinkedMDB
    sed -i "s,\$$v,${!v},g" ${fedrahome}/data/geoCoordinatesSetup/confFileGeocoordinates
    sed -i "s,\$$v,${!v},g" ${fedrahome}/data/watDivSetup/confFileWatDiv
    sed -i "s,\$$v,${!v},g" ${fedrahome}/data/watDiv100Setup/confFileWatDiv100
done

