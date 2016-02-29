#!/bin/bash

federation=$1
suffix=$2
offset=$3
i=$4

port=$((${offset}+3030))

cmdA="ld_dir('${lilachome}/data/${federation}Setup/', 'endpoint${port}.nt', 'http://${federation}Endpoint${suffix}');"
cmdB="rdf_loader_run();"
cmdC="checkpoint;"
p=$(($i+1111))

isql_cmd="isql ${p} dba"
isql_pwd="dba"

${isql_cmd} ${isql_pwd} << EOF &> /tmp/linking.log
    $cmdA
    $cmdB
    $cmdC
    exit;
EOF
