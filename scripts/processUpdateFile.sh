#!/bin/bash

file=$1

r=0

while read line; do    
   x=${line% *}
   y=${line#* }
   r=`echo "$r+$x+$y" | bc`
done < "${file}"

echo $r
