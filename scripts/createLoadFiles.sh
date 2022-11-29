#!/bin/bash

p=`pwd`

suffix=3030
federations="diseasome swdf watDiv linkedMDB geoCoordinates watDiv100"

for federation in ${federations}; do
  cd $lilachome/data/${federation}Setup/
  for i in `seq 0 9`; do
    j=$(($suffix+$i))
    echo "delete from DB.DBA.load_list;" > load${j}.isql
    echo "ld_dir ('/inputFiles', 'endpoint${j}.nt', 'http://${federation}Endpoint${i}');" >> load${j}.isql
    echo "rdf_loader_run();" >> load${j}.isql
    echo "checkpoint;" >> load${j}.isql
  done 
done

cd ${p}
