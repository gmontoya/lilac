#!/bin/bash

p=`pwd`

#suffix=3030
federations="diseasome swdf watDiv linkedMDB geoCoordinates watDiv100"
ext=ttl

for federation in ${federations}; do
  cd $lilachome/data/${federation}Setup/
  #for i in `seq 0 9`; do
    #j=$(($suffix+$i))
  echo "delete from DB.DBA.load_list;" > loadSingleEndpoint.isql
  echo "ld_dir ('/inputFiles', 'federationData.${ext}', 'http://${federation}SingleEndpoint');" >> loadSingleEndpoint.isql
  echo "rdf_loader_run();" >> loadSingleEndpoint.isql
  echo "checkpoint;" >> loadSingleEndpoint.isql
  #done 
done

cd ${p}
