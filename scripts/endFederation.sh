#!/bin/bash
file=$1

while read -u 3 host pid; do
  #echo "line: $line"
  #host=${line%% *}
  #pid=${line##* }
  address=${host#http://}
  #echo "address: $address"
  #echo "pid: $pid"
  oarsh $address "${lilachome}/scripts/endProxy.sh ${pid}"
done 3< "$file"
