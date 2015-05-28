#!/bin/bash

queriesFile=$1
hdtFile=$2
localPort=$3
queryFile=`mktemp`
tmpFile=`mktemp`
answer=`mktemp`
N=`wc -l $queriesFile | sed 's/^[ ^t]*//' | cut -d' ' -f1`

p=`pwd`
cd $fusekiHDTPath
bin/hdtEndpoint.sh --localhost --port=${localPort} --hdt=$hdtFile /ds > /dev/null &
pid=$!
sleep 10

for qn in `seq 1 $N`; do
        query=`head -n "$qn" "$queriesFile" | tail -n 1`
        (echo "$query") > $queryFile
        echo "LIMIT 1" >> $queryFile
        cd $fusekiPath
        ./s-query --service http://127.0.0.1:${localPort}/ds/query --file=${queryFile} --output=json > "$answer"
        cd $fedrahome/scripts
        python formatJSONFile.py "$answer" > "$tmpFile"
        x=`wc -l $tmpFile | sed 's/^[ ^t]*//' | cut -d' ' -f1`
        if [ $x -gt 0 ]; then
            echo $query
        fi
done

pkill -P $pid
rm $tmpFile
rm $queryFile
rm $answer
cd $p
