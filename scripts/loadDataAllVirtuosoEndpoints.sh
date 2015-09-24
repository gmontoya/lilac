#!/bin/bash

f=geoCoordinates
export PATH=.:${virtuosoPath}/bin:$PATH
i=All
mkdir ${virtuosoPath}/var/lib/virtuoso/${f}${i}
cp -r ${virtuosoPath}/var/lib/virtuoso/db/* ${virtuosoPath}/var/lib/virtuoso/${f}${i}/
sed -i "s,${virtuosoPath}/var/lib/virtuoso/db/virtuoso,${virtuosoPath}/var/lib/virtuoso/${f}${i}/virtuoso,g" ${virtuosoPath}/var/lib/virtuoso/${f}${i}/virtuoso.ini
cd ${virtuosoPath}/var/lib/virtuoso/${f}${i}
virtuoso-t -f > output 2> error &
pid=$!
sleep 2m
cd ${fedrahome}/scripts
for j in `seq 0  9`; do
    ./loadOneEndpoint.sh $f $i $j 0
    echo "${f}${j} loaded"
done
kill $pid
sleep 2m

