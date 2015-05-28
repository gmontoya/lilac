#!/bin/bash
port=$1
uniq $OAR_NODEFILE > /home/gmontoya/aux
sort /home/gmontoya/aux > /home/gmontoya/machines
rm /home/gmontoya/aux
mapfile -O 3030 -t dict < /home/gmontoya/machines
host=${dict[${port}]:-${dict[3040]}}
echo $host
