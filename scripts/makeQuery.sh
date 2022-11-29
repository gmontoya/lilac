#!/bin/bash

server=$1
query=$2
answer=$3

echo "inside makeQuery: server: $server . query: $query . answer: $answer ."

cd $fusekiPath/bin
./s-query --service $server/ds/query --file=${query} --output=json > $answer


