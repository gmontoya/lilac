templatesFile=$1 # templates file
tmpFile=`mktemp`
tmpFileAux=`mktemp`
oneTmp=`mktemp`
queriesTmp=`mktemp`
queryCount=100 # number of queries per template
cp $templatesFile ${tmpFile}
outputFile=$2 # where to put the queries
model=$3
recurrenceFactor=10
# number of lines in the templates file
r=`wc -l ${tmpFile} | cut -f1 -d' '`

while [ $r -gt 0 ]; do

  n=`grep -n "#end" ${tmpFile} |  cut -f1 -d ":" | head -n 1`
  n=$(($n-1))

  # take the first template in the file ${tmpFile}
  head -n $n ${tmpFile} > ${oneTmp}
  n=$(($n+2))

  # take the rest of the templates
  tail -n +${n} ${tmpFile} > ${tmpFileAux}
  mv ${tmpFileAux} ${tmpFile}

  cd $watdivPath/bin/Release
  ./watdiv -q ${model} ${oneTmp} $queryCount $recurrenceFactor > ${queriesTmp}
  cd $fedrahome/scripts
  # number of lines in the queries file
  r=`wc -l ${queriesTmp} | cut -f1 -d' '`
  while [ $r -gt 0 ]; do
    n=`grep -n "}" ${queriesTmp} |  cut -f1 -d ":" | head -n 1`
    head -n $n ${queriesTmp} > ${oneTmp}
    n=$(($n+1))
    tail -n +${n} ${queriesTmp} > ${tmpFileAux}
    mv ${tmpFileAux} ${queriesTmp}
    tr "\n" " " < ${oneTmp} > ${tmpFileAux}
    printf "\n" >> ${tmpFileAux}
    cat ${tmpFileAux} >> ${outputFile}
    r=`wc -l ${queriesTmp} | cut -f1 -d' '`
  done
  r=`wc -l ${tmpFile} | cut -f1 -d' '`
done
rm ${tmpFile}
rm ${queriesTmp}
rm ${tmpFileAux}
rm ${oneTmp}
