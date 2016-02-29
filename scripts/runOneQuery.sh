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
address=${15}
newFederationFile=`mktemp`
newAnapsidFederationFile=`mktemp`
newFederationFile=${newFederationFile}.ttl
tmpFile=`mktemp`
planFile=`mktemp`
queryAnswer=`mktemp`
queryFileB=`mktemp`
p=`pwd`
to=1800

source ${lilachome}/scripts/export.sh
n=`shuf -i 1-100 -n 1`
cd ${lilachome}/code
ntp=`java -cp .:${jenaPath}/lib/* obtainTriples $queryFile | wc -l`
shape=`java -cp .:${jenaPath}/lib/* getQueryShape $queryFile`
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
    if [ "$action" != "justExecute" ] && [ "$action" != "justSelect" ]; then
      bash ./updateFederation.sh $queryFile http://${address}:${localPort}/ds $ldfServer $configFile $federationFile $newFederationFile $anapsidFederationFile $newAnapsidFederationFile $updatesFile http://${address}:${localProxyPort}/ds ${publicEndpoint}
    fi
    if [ "$engine" = "FedX11" ] && [ "$action" != "justReplicate" ]; then
        cd ${lilachome}/code
        source $configFile
        rm $tmpFile
        for i in `seq 1 2`; do
            /usr/bin/time -f "%e %S %U" java -cp ".:${jenaPath}/lib/*" fedra2 $queryFile $FragmentsDefinitionFolder $EndpointsFile $FragmentsSources $Random $queryFileB $availableSources 2> $tmpFile
        done
        #echo "query with service"
        #echo $queryFileB
        sst=`tail -n 1 $tmpFile` 
        cd ${fedXPath}
        rm $cacheLocation
        /usr/bin/time -f "$query $sst %e %S %U %P %t %M" timeout ${to} ./cli.sh  -d $newFederationFile -f JSON -folder results @q $queryFileB > $planFile 2> $tmpFile
        #cat ${fedXPath}/results/results/q_1.json
        if [ -f "${fedXPath}/results/results/q_1.json" ]; then
            mv ${fedXPath}/results/results/q_1.json $queryAnswer
        else
            rm $queryAnswer
            touch $queryAnswer
        fi
        #echo "query plan"
        #cat $planFile
        #echo "query answer"
        #cat $queryAnswer
        #echo "error file"
        #cat $tmpFile
        z=`grep "^ERROR:" $tmpFile`
        w=`grep "^Exception" $tmpFile`
        if [ -z "$z" ] && [ -z "$w" ]; then
            cd "$p"
            x=`less $tmpFile`
            ./processJSONAnswer.sh $queryAnswer $groundTruth > $tmpFile
            y=`less $tmpFile`
            cd ${lilachome}/code
            nss=`java -cp ".:${jenaPath}/lib/*" VisitorCountTriples $queryFileB`
            echo "$x $y $nss $ntp $shape"
        else
            echo "$query ERROR"
            cat $tmpFile
        fi
    fi
    if [ "$engine" = "FedX" ] && [ "$action" != "justReplicate" ]; then
        if  [ "$action" = "justSelect" ]; then
            planOnly="-planOnly"
        else
            planOnly=""
        fi
        cd ${fedXPath}
        source $configFile
        rm $cacheLocation
        rm ${fedXPath}/results/results/*
        /usr/bin/time -f "$query %e %S %U %P %t %M" timeout ${to} ./cli.sh $planOnly -c $configFile -d $newFederationFile -f JSON -folder results @q $queryFile > $planFile 2> $tmpFile
        if [ -f "${fedXPath}/results/results/q_1.json" ]; then
              mv ${fedXPath}/results/results/q_1.json $queryAnswer
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
            cat $tmpFile
        fi
    fi
    if [ "$engine" = "ANAPSID11" ] && [ "$action" != "justReplicate" ]; then
        cd ${lilachome}/code
        source $configFile
        /usr/bin/time -f "%e %S %U" java -cp ".:${jenaPath}/lib/*" fedra2 $queryFile $FragmentsDefinitionFolder $EndpointsFile $FragmentsSources $Random $queryFileB $availableSources 2> $tmpFile
        sst=`tail -n 1 $tmpFile`
        cd ${lilachome}
        m=`free | awk 'NR==2{printf "%s", $2 }'`
        m=`echo "scale=0; $m*9/10" | bc`
        (ulimit -m $m; ulimit -v $m; /usr/bin/time -f "$query $sst %e %S %U %P %t %M" timeout -s 12 ${to}s ${anapsidPath}/scripts/run_anapsid -e $newAnapsidFederationFile -q $queryFileB  -s False -p b -o True -d SSGM -a True -w False -r True -f $queryAnswer -k b > $planFile 2> $tmpFile) 
        #/usr/bin/time -f "$query $sst %e %S %U %P %t %M" timeout -s 12 ${to}s ${anapsidPath}/scripts/run_anapsid -e $newAnapsidFederationFile -q $queryFileB  -s False -p b -o True -d SSGM -a True -w False -r True -f $queryAnswer -k b > $planFile 2> $tmpFile
        x=`less $tmpFile`
        cd ${lilachome}/scripts
        ./processANAPSIDAnswer.sh $queryAnswer $groundTruth > $tmpFile
        y=`less $tmpFile`
        cd ${lilachome}/code
        nss=`java -cp ".:${jenaPath}/lib/*" VisitorCountTriples $planFile`
        echo "$x $y $nss $ntp $shape"
        cd $p
    fi
    if [ "$engine" = "ANAPSID" ] && [ "$action" != "justReplicate" ]; then
        if  [ "$action" = "justSelect" ]; then
          decomposition=d
        else
          decomposition=b
        fi
        cd ${lilachome}
        m=`free | awk 'NR==2{printf "%s", $2 }'`
        m=`echo "scale=0; $m/3.43" | bc`
        rm $queryAnswer $planFile $tmpFile
        (ulimit -m $m; ulimit -v $m; /usr/bin/time -f "$query %e %S %U %P %t %M" timeout -s 12 ${to}s ${anapsidPath}/scripts/run_anapsid -e $newAnapsidFederationFile -q $queryFile -c $configFile -s False -p ${decomposition} -o False -d SSGM -a True -w False -r True -f $queryAnswer -k b > $planFile 2> $tmpFile)
        x=`less $tmpFile`
        cd ${lilachome}/scripts
        ./processANAPSIDAnswer.sh $queryAnswer $groundTruth > $tmpFile
        y=`less $tmpFile`
        cd ${lilachome}/code
        #echo "planFile: $planFile"
        #cat $planFile
        nss=`java -cp ".:${jenaPath}/lib/*" VisitorCountTriples $planFile`
        echo "$x $y $nss $ntp $shape"
        cd $p
    fi 
fi
if [ "$strategy" = "LDF" ]; then
    /usr/bin/time -f "$query %e %S %U %P %t %M" timeout ${to} ldf-client $ldfServer $queryFile -t application/sparql-results+json > $queryAnswer 2> $tmpFile
    x=`less $tmpFile`
    ./processJSONAnswer.sh $queryAnswer $groundTruth > $tmpFile
    y=`less $tmpFile`
    echo "$x $y $ntp $shape"
fi
if [ "$strategy" = "PUBLIC" ]; then
    if [ $n -gt $availability ]; then
      (echo "{ \"results\" : { \"bindings\" : [ ] } }") > $queryAnswer
      x="0 0% 0 0"

    else 
      cd ${fusekiPath}
      /usr/bin/time -f "$query %e %S %U %P %t %M" timeout ${to} ./s-query --service ${publicEndpoint} --file=${queryFile} --output=json > $queryAnswer 2> $tmpFile
      x=`less $tmpFile`
    fi
    cd $p
    ./processJSONAnswer.sh $queryAnswer $groundTruth > $tmpFile
    y=`less $tmpFile`
    echo "$x $y $ntp $shape"
fi
if [ "$strategy" = "LOCAL" ]; then
    cd ${fusekiPath}
    /usr/bin/time -f "$query %e %S %U %P %t %M" timeout ${to} ./s-query --service http://localhost:${localPort}/ds/query --file=${queryFile} --output=json > $queryAnswer 2> $tmpFile
    x=`less $tmpFile`
    cd $p
    ./processJSONAnswer.sh $queryAnswer $groundTruth > $tmpFile
    y=`less $tmpFile`
    echo "$x $y $ntp $shape"
fi

rm $queryAnswer
rm $tmpFile
rm $planFile
rm $newAnapsidFederationFile
