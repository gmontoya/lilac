#!/bin/bash

file=$1
l=`cat ${file}`
l="$l"
queriesFile=$2
host=$3
localPort=$4
answersFolder=$5

queryFile=`mktemp`
tmpFile=`mktemp`

for qn in $l; do
    if [ ! -f $answersFolder/query${qn} ]; then
        (head -n "$qn" "$queriesFile" | tail -n 1) > $queryFile
        cd $fusekiPath/bin
        ./s-query --service http://${host}:${localPort}/sparql --file=${queryFile} --output=json > "$answersFolder/query${qn}"
        cd $lilachome/scripts
        python formatJSONFile.py "$answersFolder/query${qn}" > "$tmpFile"
        LANG=En_US sort "$tmpFile" > "$answersFolder/query${qn}"
    fi
done

rm $tmpFile
rm $queryFile
