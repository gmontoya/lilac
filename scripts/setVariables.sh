#!/bin/bash

lilachome=${lilachome}
fusekiPath=${fusekiPath}
dawIndexGeneratorPath=${dawIndexGeneratorPath}
fusekiHDTPath=${fusekiHDTPath}
httpcomponentsClientPath=${httpcomponentsClientPath}
watdivPath=${watdivPath}
fedXPath=${fedXPath}
jenaPath=${jenaPath}
anapsidPath=${anapsidPath}
virtuosoPath=${virtuosoPath}
hdtJavaPath=${hdtJavaPath}

variables="lilachome fusekiPath dawIndexGeneratorPath fusekiHDTPath httpcomponentsClientPath watdivPath fedXPath jenaPath anapsidPath virtuosoPath hdtJavaPath"

for v in $variables; do
    sed -i "s,\${$v},${!v},g" *.sh
    sed -i "s,\$$v,${!v},g" *.sh
    sed -i "s,\$$v,${!v},g" ${lilachome}/data/diseasomeSetup/confFileDiseasome
    sed -i "s,\$$v,${!v},g" ${lilachome}/data/swdfSetup/confFileSWDF
    sed -i "s,\$$v,${!v},g" ${lilachome}/data/linkedMDBSetup/confFileLinkedMDB
    sed -i "s,\$$v,${!v},g" ${lilachome}/data/geoCoordinatesSetup/confFileGeocoordinates
    sed -i "s,\$$v,${!v},g" ${lilachome}/data/watDivSetup/confFileWatDiv
    sed -i "s,\$$v,${!v},g" ${lilachome}/data/watDiv100Setup/confFileWatDiv100
done

