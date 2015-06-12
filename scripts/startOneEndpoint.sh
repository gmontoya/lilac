#!/bin/bash

f=$1
i=$2
host=$3

${fedrahome}/scripts/startEndpoint.sh $f $i $host &

