#!/bin/bash

planFile=`mktemp`
queryFile=$1
fusekiEndpoint=$2
ldfServer=$3
configFile=${4}
constructQuery=`mktemp`
fragment=`mktemp`
inside=false
str=""
threshold=3
ssq=false
federationFile=$5
newFederationFile=$6
anapsidFederationFile=$7
newAnapsidFederationFile=$8
updatesFile=$9
proxy=${10}
publicEndpoint=${11}
tmpFile=`mktemp`

parse() {
   str="$1"
   #echo "str: $str"
   isConstant=false
   if [ "${str:10:6}" = "-const" ]; then 
       isConstant=true
       b="value="
       str=`echo $str | grep -o "$b.*"`
       str=${str:6:-12}
       if [ "${str:1:1}" != "\"" ]; then
          str="<$str>"
       fi
   else
       b="name="
       str=`echo $str | grep -o "$b.*"`
       str=`echo ${str%%)*}`
       str=`echo ${str%%-*}`
       str=${str:5}
       str="?$str"
   fi
   #echo "parsed str: $str"
}

action() {
  ldf-client $ldfServer $constructQuery > ${fragment}.n3
  cd ${lilachome}/code
  java -cp .:${jenaPath}/lib/* LoadFragment2 ${fragment}.n3 ${fusekiEndpoint}/update > $tmpFile
  #cat $tmpFile
  #echo "n: $n"
  #echo "configFile: $configFile"
  #echo "UF constructQuery: $constructQuery"
  #echo "thread: $$ before updating fedra files"
  ./updateFedraFiles.sh $configFile $constructQuery ${proxy}/sparql $updatesFile $publicEndpoint
  k=`grep "$proxy" $federationFile | wc -l | sed 's/^[ ^t]*//' | cut -d' ' -f1`
  if [ "$k" -eq 0 ]; then
    (echo "") >> $federationFile
    (echo "<$proxy> fluid:store \"SPARQLEndpoint\";") >> $federationFile
    (echo "fluid:SPARQLEndpoint \"${proxy}/sparql\".") >> $federationFile
    (echo "") >> $newFederationFile
    (echo "<$proxy> fluid:store \"SPARQLEndpoint\";") >> $newFederationFile
    (echo "fluid:SPARQLEndpoint \"${proxy}/sparql\".") >> $newFederationFile
  fi
  updateAnapsidDescriptions
  #echo "thread: $$ after updating fedra files"
}

updateAnapsidDescriptions() {
  # if there is an anapsid endpoint description, it should be updated too
  nt=`wc -l ${fragment}.n3 | sed 's/^[ ^t]*//' | cut -d' ' -f1`
  if [ -f "$anapsidFederationFile" ] && [ "$nt" -gt "0" ]; then
    # check if there is already information about the endpoint
    m=`grep "<${proxy}/sparql>" "$anapsidFederationFile"`
    if [ -z "$m" ]; then
        m="<${proxy}/sparql> . "
    fi
    #echo "n: $n"
    # extract the predicate
    #echo ${triple}
    triple=`echo ${triple##*( )}`
    po=`echo ${triple#* }`
    #echo $po
    predicate=`echo ${po%% *}`
    #echo $predicate
    # if the predicate was not there, it should be included
    if [[ $m != *"${predicate}"* ]]; then
        m=`echo ${m%.*}` # remove final dot
        m="$m ${predicate} . " # include predicate and put final dot
        #echo "new n: $n"
        grep -v "<${proxy}/sparql>" "$anapsidFederationFile" > $tmpFile
        (echo "$m") >> $tmpFile
        mv $tmpFile $anapsidFederationFile
        grep -v "<${proxy}/sparql>" "$newAnapsidFederationFile" > $tmpFile
        (echo "$m") >> $tmpFile
        mv $tmpFile $newAnapsidFederationFile
    fi
  fi
}

triple=""
included=false

n=`grep "fluid:SPARQLEndpoint" ${newFederationFile} | wc -l | sed 's/^[ ^t]*//' | cut -d' ' -f1`

source $configFile
p=`pwd`
cd ${lilachome}/code
java -cp .:${jenaPath}/lib/* obtainTriples $queryFile > $planFile
while read line; do 
    triple="${line}"
    query="CONSTRUCT WHERE { $triple }"
    #echo "$query"
    (echo "$query") > $constructQuery
    cd ${lilachome}/code
    numReplicas=`java -cp .:${jenaPath}/lib/* countRelevantFragments $constructQuery ${FragmentsDefinitionFolder} ${EndpointsFile}`
    #echo "numReplicas: ${numReplicas}"
    if [ "${numReplicas}" -lt "$threshold" ]; then
        action
        #updateAnapsidDescriptions
    fi
done < $planFile

cd $p

rm $constructQuery
rm ${fragment}.n3
rm $planFile
rm $tmpFile
