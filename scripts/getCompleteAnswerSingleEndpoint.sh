#!/bin/bash

offset=0
file=$1
address=$2
# expected N-Triples for Construct queries and json for Select queries
outputFormat=$3
outputFile=$4
limit=20000

queryFile=`mktemp`
tmpFile=`mktemp`
tmpFile2=`mktemp`

rm -f $outputFile
touch $outputFile
p=`pwd`

queryEndpointSelect() {
  cp $file $queryFile
  (echo "OFFSET $offset") >> $queryFile
  (echo "LIMIT $limit") >> $queryFile
  cat $queryFile

  #./s-query --service $address --file=${queryFile} --output=${outputFormat} > $tmpFile 
  ./rsparql --service $address --file ${queryFile} --results=${outputFormat} > $tmpFile
  python $lilachome/scripts/formatJSONFile.py $tmpFile > $tmpFile2
  mv $tmpFile2 $tmpFile

  ##echo "tmpFileSize: $tmpFile"
  ##wc -l $tmpFile
  ###mv $tmpFile $tmpFile2
  ###./ntriples --syntax=${outputFormat} $tmpFile2 > $tmpFile
  ##echo "tmpFileSize: $tmpFile"
  ##wc -l $tmpFile
  LANG=En_US sort $tmpFile | grep -v "^\#" > $tmpFile2

  newTriplesNumber=`LANG=En_US comm -23 $tmpFile2 $outputFile | wc -l | sed 's/^[ ^t]*//' | cut -d' ' -f1`
  ##echo "newTriplesNumber: $newTriplesNumber"
  ##echo "new triples in: $tmpFile2"
  if [ "$newTriplesNumber" -gt "0" ]; then    
    cp $outputFile $tmpFile
    cat $tmpFile2 >> $tmpFile
    LANG=En_US sort $tmpFile | uniq > $outputFile

    offset=$(($offset+$limit))
    queryEndpointSelect
  fi
}

queryEndpointConstruct() {
  cp $file $queryFile
  (echo "OFFSET $offset") >> $queryFile
  (echo "LIMIT $limit") >> $queryFile
  cat $queryFile
  cd $jenaPath/bin
  ./rsparql --service $address --file ${queryFile} --results=${outputFormat} > $tmpFile2

  cd $lilachome/code
  newTriplesNumber=`java -cp .:$jenaPath/lib/* combineModels $outputFile $tmpFile2 $tmpFile`
  if [ "$newTriplesNumber" -gt "0" ]; then
    mv $tmpFile $outputFile
    offset=$(($offset+$limit))
    queryEndpointConstruct
  fi
}

#cd $fusekiPath/bin
cd $jenaPath/bin
if [ "$outputFormat" = "json" ]; then
    queryEndpointSelect
else
    tmpFile=${tmpFile}.nt
    tmpFile2=${tmpFile2}.nt
    touch $tmpFile
    touch $tmpFile2
    queryEndpointConstruct
fi

#rm -f $tmpFile
#rm -f $tmpFile2
echo "$tmpFile"
echo "$tmpFile2"
rm -f $queryFile
cd ${p}
