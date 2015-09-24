#!/bin/bash

federations="linkedMDB geoCoordinates watDiv100"
export PATH=.:${virtuosoPath}/bin:$PATH

for i in `seq 4 9`; do
  for f in $federations; do    
    mkdir ${virtuosoPath}/var/lib/virtuoso/${f}${i}
    cp -r ${virtuosoPath}/var/lib/virtuoso/db/* ${virtuosoPath}/var/lib/virtuoso/${f}${i}/
    sed -i "s,${virtuosoPath}/var/lib/virtuoso/db/virtuoso,${virtuosoPath}/var/lib/virtuoso/${f}${i}/virtuoso,g" ${virtuosoPath}/var/lib/virtuoso/${f}${i}/virtuoso.ini
    b=8890
    p=$(($b+i))
    sed -i "s,8890,$p,g" ${virtuosoPath}/var/lib/virtuoso/${f}${i}/virtuoso.ini
    b=1111
    p=$(($b+i))
    sed -i "s,1111,$p,g" ${virtuosoPath}/var/lib/virtuoso/${f}${i}/virtuoso.ini
    cd ${virtuosoPath}/var/lib/virtuoso/${f}${i}
    virtuoso-t -f > output 2> error &
    pid=$!
    sleep 2m
    cd ${fedrahome}/scripts
    ./loadOneEndpoint.sh $f $i $i $i
    echo "${f}${i} loaded"
    kill $pid
    sleep 2m
  done
done
