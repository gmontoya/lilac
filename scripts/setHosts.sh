#!/bin/bash
f=$1
firstPort=$2
publicEndpointPort=$3
# 8890 for virtuoso endpoints 3130 for proxies
# 8900 for VE 3100 for proxies

#ports="3100 3130 3131 3132 3133 3134 3135 3136 3137 3138 3139"

for i in `seq 0 9`; do
    port=$(($i+3130))
    p=$(($i+8890))
    np=$(($i+$firstPort))
    #p=$(($port-100))
    address=`./getHost.sh ${lilachome}/data/${f}Setup/hosts ${p}`
    host=http://$address
    files="${lilachome}/data/${f}Setup/endpointsDescription ${lilachome}/data/${f}Setup/federation.ttl ${lilachome}/data/${f}Setup/dawIndex.ttl ${lilachome}/data/${f}Setup/fedraFiles/endpoints"
    for file in $files; do
        sed -i "s,HOST:$port,${host}:${np},g" $file
    done
done

port=3100
p=8900
np=$publicEndpointPort

address=`./getHost.sh ${lilachome}/data/${f}Setup/hosts ${p}`
host=http://$address
files="${lilachome}/data/${f}Setup/endpointsDescription ${lilachome}/data/${f}Setup/federation.ttl ${lilachome}/data/${f}Setup/dawIndex.ttl ${lilachome}/data/${f}Setup/fedraFiles/endpoints"
for file in $files; do
    sed -i "s,HOST:$port,${host}:${np},g" $file
done

