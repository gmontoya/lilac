#!/bin/bash

federation=$1
i=$2

port=$(($i+3030))

cmdA="ld_dir('${fedrahome}/data/${federation}Setup/', 'endpoint${port}.nt', 'http://${federation}Endpoint${i}');"
cmdB="rdf_loader_run();"
cmdC="checkpoint;"
p=$(($i+1111))

isql_cmd="isql ${p} dba"
isql_pwd="dba"

${isql_cmd} ${isql_pwd} << EOF &> /home/gmontoya/tmp/linking.log
    $cmdA
    $cmdB
    $cmdC
    exit;
EOF
