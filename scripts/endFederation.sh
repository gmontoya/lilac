#!/bin/bash
file=$1

while read line; do
  host=${line%% *}
  pid=${line##* }
  address=${host#http://}
  oarsh $address "${fedrahome}/scripts/endProxy.sh $pid"
done < "$file"
