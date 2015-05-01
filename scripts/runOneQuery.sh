#!/bin/bash

strategy=$1
queryFile=$2
localPort=$3
ldfServer=$4
federationFile=$5
availability=$6
groundTruth=$7
publicEndpoint=${8}
engine=${9}
configFile=${10}
anapsidFederationFile=${11}
updatesFile=${12}
localProxyPort=${13}
action=${14}
newFederationFile=`mktemp`
newAnapsidFederationFile=`mktemp`
newFederationFile=${newFederationFile}.ttl
tmpFile=`mktemp`
planFile=`mktemp`
queryAnswer=`mktemp`
queryFileB=`mktemp`
p=`pwd`
to=1800

n=`shuf -i 1-100 -n 1`
cd $fedrahome/code
ntp=`java -cp .:$jenaPath/lib/* obtainTriples $queryFile | wc -l`
shape=`java -cp .:$jenaPath/lib/* getQueryShape $queryFile`
cd $p
#echo "new fed file: $newFederationFile"
if [ $n -gt $availability ]; then
    #echo "removing public endpoint, value of n: $n"
    pep=${publicEndpoint%%/sparql}
    grep -iv "$pep" $federationFile > $newFederationFile
    #grep -i "$pep" $federationFile > $tmpFileB
    grep -iv "<${publicEndpoint}>" $anapsidFederationFile > $newAnapsidFederationFile
    #grep -i "<${publicEndpoint}>" $anapsidFederationFile > $tmpFileC
else 
    cp $federationFile $newFederationFile
    cp $anapsidFederationFile $newAnapsidFederationFile
fi
availableSources=`mktemp`
grep "fluid:SPARQLEndpoint" $newFederationFile | while read -r line; do
   x=${line%\"*}
   x=${x#*\"}
   (echo $x) >> $availableSources
done
query=`echo "${groundTruth##*/}"`
if [ "$strategy" = "FEDERATION" ]; then
    # update endpoint should update also the fedra files, and possibly include this endpoint in the federationFile
    if [ "$action" != "justExecute" ]; then
      bash ./updateFederation.sh $queryFile $host:${localPort}/ds $ldfServer $configFile $federationFile $newFederationFile $anapsidFederationFile $newAnapsidFederationFile $updatesFile $host:${localProxyPort}/ds ${publicEndpoint}
    fi
    if [ "$engine" = "FedX" ] && [ "$action" != "justReplicate" ]; then
        cd $fedXPath
          rm cache.db
          /usr/bin/time -f "$query %e %P %t %M" timeout ${to} ./cli.sh -c $configFile -d $newFederationFile -f JSON -folder results @q $queryFile > $planFile 2> $tmpFile
          if [ -f "$fedXPath/results/results/q_1.json" ]; then
              mv $fedXPath/results/results/q_1.json $queryAnswer
          else
              rm $queryAnswer
              touch $queryAnswer
          fi
        z=`grep "^ERROR:" $tmpFile`
        w=`grep "^Exception" $tmpFile`
        if [ -z "$z" ] && [ -z "$w" ]; then
            cd "$p"
            x=`less $tmpFile`
            ./processJSONAnswer.sh $queryAnswer $groundTruth > $tmpFile
            y=`less $tmpFile`

            pe=${publicEndpoint#http://}
            pe=${pe%%/*}
            nss=`grep "StatementSource (" $planFile | wc -l | sed 's/^[ ^t]*//' | cut -d' ' -f1`
            if [ $nss -eq "0" ]; then
                a=`grep "SingleSourceQuery" $planFile`
                if [ -n "$a" ]; then
                    nss=`grep "StatementPattern" $planFile | wc -l | sed 's/^[ ^t]*//' | cut -d' ' -f1`
                fi
            fi
            echo "$x $y $nss $ntp $shape"
        else 
            echo "$query ERROR"
        fi
    fi
    if [ "$engine" = "ANAPSID" ] && [ "$action" != "justReplicate" ]; then
        /usr/bin/time -f "$query %e %P %t %M" timeout -s 12 ${to}s $anapsidPath/scripts/run_anapsid -e $newAnapsidFederationFile -q $queryFile -c $configFile -s False -p b -o False -d SSGM -a True -w False -r True -f $queryAnswer -k b > $planFile 2> $tmpFile
        x=`less $tmpFile`
        cd $fedrahome/scripts
        ./processANAPSIDAnswer.sh $queryAnswer $groundTruth > $tmpFile
        y=`less $tmpFile`
        cd $fedrahome/code
        nss=`java -cp ".:$jenaPath/lib/*" VisitorCountTriples $planFile`
        echo "$x $y $nss $ntp $shape"
        cd $p
    fi 
fi

rm $queryAnswer
rm $tmpFile
rm $planFile
rm $newAnapsidFederationFile
