#!/bin/bash

configFile=$1
constructQuery=$2
endpoint=$3
updatesFile=$4
pe=$5

source $configFile
tmpFile=`mktemp`

cd $fedrahome/code
viewName=`java -cp ".:$jenaPath/lib/*" updateFedraFiles $constructQuery  ${FragmentsDefinitionFolder} ${FragmentsSources} ${endpoint} ${pe} $updatesFile` 
x=`grep -w "^${viewName}" "$EndpointsFile"`
if [ -z "$x" ]; then
    x=`echo ${viewName}`
else
    grep -v "$x" "$EndpointsFile" > $tmpFile
    mv $tmpFile $EndpointsFile
fi
aa=`grep -w "${endpoint}" <<< ${x}`
if [ -z "${aa}" ]; then
    x="$x ${endpoint}"
fi
aa=`grep -w "${pe}" <<< ${x}`
if [ -z "${aa}" ]; then
    x="$x ${pe}"
fi
(echo "$x") >> ${EndpointsFile}
#fi
