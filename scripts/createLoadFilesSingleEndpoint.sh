#!/bin/bash

p=`pwd`

federations="diseasome swdf watDiv linkedMDB geoCoordinates watDiv100"
ext=nt

for federation in ${federations}; do
  cd $lilachome/data/${federation}Setup/
  echo "delete from DB.DBA.load_list;" > loadSingleEndpoint.isql
  echo "ld_dir ('/inputFiles', 'federationData.${ext}', 'http://${federation}SingleEndpoint');" >> loadSingleEndpoint.isql
  echo "rdf_loader_run();" >> loadSingleEndpoint.isql
  echo "checkpoint;" >> loadSingleEndpoint.isql
done

cd ${p}
