#!/bin/bash

l=$1
queriesFile=$2
hdtFile=$3
localPort=$4
answersFolder=$5
queryFile=`mktemp`
tmpFile=`mktemp`

cd ${fusekiHDTPath}
bin/hdtEndpoint.sh --localhost --port=${localPort} --hdt=$hdtFile /ds > /dev/null &
pid=$!
sleep 10

for qn in $l; do
    if [ ! -f $answersFolder/query${qn} ]; then
        (head -n "$qn" "$queriesFile" | tail -n 1) > $queryFile
        cd $fusekiPath/bin
        ./s-query --service http://127.0.0.1:${localPort}/ds/query --file=${queryFile} --output=json > "$answersFolder/query${qn}"
        cd $lilachome/scripts
        python formatJSONFile.py "$answersFolder/query${qn}" > "$tmpFile"
        LANG=En_US sort "$tmpFile" > "$answersFolder/query${qn}"
    fi
done

pkill -P $pid
rm $tmpFile
rm $queryFile
