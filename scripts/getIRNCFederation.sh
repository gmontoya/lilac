#!/bin/bash

prefix=$1
firstPort=$2
lastPort=$3

ir=0
nc=0
for i in  `seq $firstPort $lastPort`; do
   line=`tail -n 1 $prefix$i`
   x=${line% *}
   y=${line#* }
   ir=$(($ir+$x))
   nc=$(($nc+$y))  
done

if [ -f "${prefix}PublicEndpoint" ]; then
  line=`tail -n 1 ${prefix}PublicEndpoint`
  x=${line% *}
  y=${line#* }
  ir=$(($ir+$x))
  nc=$(($nc+$y)) 
fi

echo "$ir $nc"
