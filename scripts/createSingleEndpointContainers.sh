#!/bin/bash

federations="diseasome swdf watDiv linkedMDB geoCoordinates watDiv100"

# for 64GB available
NumberOfBuffers=5450000
MaxDirtyBuffers=4000000
mem="64g"
# for 16GB available
NumberOfBuffers=1360000
MaxDirtyBuffers=1000000
mem="16g"
# for 2GB available
NumberOfBuffers=170000
MaxDirtyBuffers=130000
mem="2g"

ResultSetMaxRows=100000
MaxQueryCostEstimationTime=6000
MaxQueryExecutionTime=6000

storageFolder=$dockerEndpointFolder

i=10 # offset available ports, use 10 for a federation of 10 endpoints

for federation in ${federations}; do

  #for i in `seq 0 9`; do
    cd $storageFolder
    #j=$(($i+$suffix))
    name=${federation}SingleEndpoint
    pw=passwordep${i}
    portA=$((1111+$i))
    portB=$((8890+$i))
    rm -rf ${name}
    mkdir ${name}
    cd ${name}
    docker create \
      --name ${name} \
      --cpus="1" \
      --memory=${mem} \
      --env DBA_PASSWORD=${pw} \
      --env VIRT_Parameters_DirsAllowed="., ../vad, /inputFiles" \
      --env VIRT_Parameters_NumberOfBuffers=${NumberOfBuffers} \
      --env VIRT_Parameters_MaxDirtyBuffers=${MaxDirtyBuffers} \
      --env VIRT_SPARQL_ResultSetMaxRows=${ResultSetMaxRows} \
      --env VIRT_SPARQL_MaxQueryCostEstimationTime=${MaxQueryCostEstimationTime} \
      --env VIRT_SPARQL_MaxQueryExecutionTime=${MaxQueryExecutionTime} \
      --publish ${portA}:1111 \
      --publish ${portB}:8890 \
      --volume `pwd`:/database \
      --volume $lilachome/data/${federation}Setup:/inputFiles \
          openlink/virtuoso-opensource-7:latest &
    PID=$!
    echo $PID
    sleep 10
  #done
done

