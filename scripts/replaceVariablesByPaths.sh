source variables

federations="watDiv diseasome swdf linkedMDB geoCoordinates watDiv100"

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
