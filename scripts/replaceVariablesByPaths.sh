source variables


#fs="$lilachome/data/diseasomeSetup/confFileDiseasome $lilachome/data/swdfSetup/confFileSWDF $lilachome/data/linkedMDBSetup/confFileLinkedMDB $lilachome/data/geoCoordinatesSetup/confFileGeocoordinates $lilachome/data/watDivSetup/confFileWatDiv $lilachome/data/watDiv100Setup/confFileWatDiv100 $lilachome/data/diseasomeSetup/fuseki.ttl $lilachome/data/swdfSetup/fuseki.ttl $lilachome/data/linkedMDBSetup/fuseki.ttl $lilachome/data/geoCoordinatesSetup/fuseki.ttl $lilachome/data/watDivSetup/fuseki.ttl $lilachome/data/watDiv100Setup/fuseki.ttl"

for v in $variables; do
    sed -i "s,\${$v},${!v},g" *.sh
    sed -i "s,\$$v,${!v},g" *.sh
    #for f in $fs; do
    for federation in ${federations}; do
        f=$lilachome/data/${federation}Setup/confFile
        sed -i "s,\$$v,${!v},g" $f
        f=$lilachome/data/${federation}Setup/fuseki.ttl
        sed -i "s,\$$v,${!v},g" $f
    done
done
