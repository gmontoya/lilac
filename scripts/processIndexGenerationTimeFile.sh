#!/bin/bash

file=$1

r=0

while read line; do    
   r=`echo "$r+$line" | bc`
done < "${file}"

echo $r
