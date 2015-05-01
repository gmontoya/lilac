#!/bin/bash

fedrahome=FEDRAHOME
fusekiPath=FUSEKIPATH
dawIndexGeneratorPath=DAWINDEXGENERATORPATH
host="HOSTADDRESS"
fusekiHDTPath=HDTFUSEKIPATH
httpcomponentsClientPath=HTTPCOMPONENTSPATH
watdivPath=WATDIVPATH
fedXPath=FEDXPATH
jenaPath=JENAPATH
anapsidPath=ANAPSIDPATH

variables="fedrahome fusekiPath dawIndexGeneratorPath host fusekiHDTPath httpcomponentsClientPath watdivPath fedXPath jenaPath anapsidPath"
address=${host#http://}

for v in $variables; do
    sed -i "s,\${$v},${!v}," *.sh
    sed -i "s,\$$v,${!v}," *.sh
    sed -i "s,\$$v,${!v}," $fedrahome/data/diseasomeSetup/confFileDiseasome
    sed -i "s,\$$v,${!v}," $fedrahome/data/swdfSetup/confFileSWDF
    sed -i "s,\$$v,${!v}," $fedrahome/data/linkedMDBSetup/confFileLinkedMDB
    sed -i "s,\$$v,${!v}," $fedrahome/data/geoCoordinatesSetup/confFileGeocoordinates
    sed -i "s,\$$v,${!v}," $fedrahome/data/watDivSetup/confFileWatDiv
    sed -i "s,\$$v,${!v}," $fedrahome/data/watDiv100Setup/confFileWatDiv100
done

sed -i".bkp" "s,address=.*,address=${address}," *.sh
mv setVariables.sh.bkp setVariables.sh

rm *.bkp
