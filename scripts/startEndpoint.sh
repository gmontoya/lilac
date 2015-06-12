#!/bin/bash
export PATH=.:${virtuosoPath}/bin:$PATH
f=$1
i=$2
host=$3
cd ${virtuosoPath}/var/lib/virtuoso/${f}${i}
#pwd
virtuoso-t -f > output 2> error &
pid=$!
echo "$host $pid"
#sleep 2m
