#!/bin/bash
hosts=$1
port=$2

mapfile -O 3030 -t dict < $hosts

defaultValue=${dict[-1]}

host=${dict[${port}]:-${defaultValue}}
echo $host
