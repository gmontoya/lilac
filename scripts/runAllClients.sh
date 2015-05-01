#!/bin/bash

strategies=$1 #FEDERATION LDF PUBLIC LOCAL
numberClients=$2
queriesFile=$3
numberQueries=$4
firstPort=$5
ldfServer=$6
federationFile=$7
availability=$8
answersFolder=$9
port=$firstPort
publicEndpoint=${10}
engines=${11}
configFile=${12}
hdtFile=${13}
anapsidFederationFile=${14}
updatesFile=${15}
firstProxyPort=${16}
sourceSelectionStrategy=${17}
action=${18} # one of justReplicate justExecute all
queriesToExecute=${19}
publicEndpointPort=${20}
publicEndpointProxyPort=${21}
setupFolder=${22}
tmpFilePEP=`mktemp`
lastPort=$(($firstPort+$numberClients-1))
last=$(($numberClients-1))

N=`wc -l $queriesFile | sed 's/^[ ^t]*//' | cut -d' ' -f1`

if [ ! -f "$queriesToExecute" ]; then
    queriesToExecute=`mktemp`
    touch "$queriesToExecute"
    echo "queries to execute saved in: ${queriesToExecute}"
fi

for i in  `seq 0 $last`; do
    f=${queriesToExecute}_${i}
    if [ -f "$f" ]; then
        l[${i}]=`cat ${queriesToExecute}_${i}`
    else
        l[${i}]=`shuf -i 1-$N -n $numberQueries`
        (echo "${l[i]}") > ${queriesToExecute}_${i}
        if [ "$action" != "justReplicate" ]; then
            ./produceAnswer.sh "${l[i]}" "$queriesFile" "$hdtFile" "${firstProxyPort}" "$answersFolder"
        fi
    fi
done

for strategy in $strategies; do
    spids=""
    #for port in `seq $firstPort $lastPort`; do
    if [ "$strategy" != "FEDERATION" ] || [ "$action" = "justReplicate" ]; then
        es="_"
        sss="_"
    else
        es=$engines
        sss=$sourceSelectionStrategy
    fi
    echo "es: $es . sss: $sss"
    for engine in $es; do
      for ss in $sss; do
        if [ "$strategy" = "FEDERATION" ] || [ "$strategy" = "PUBLIC" ]; then
          if [ "$action" != "justReplicate" ]; then
            p=`pwd`
            cd $fedrahome/proxy
            address=${host#http://}
            java -cp .:$httpcomponentsClientPath/lib/* SingleEndpointProxy2 $address ${publicEndpointPort} ${publicEndpointProxyPort} > $tmpFilePEP &
            pidPEProxy=$!
            cd $p
          fi
        fi
        sed -i".bkp" "s/SourceSelectionStrategy=[a-zA-Z_]*/SourceSelectionStrategy=${ss}/" ${configFile}
        label="$engine${ss}$strategy"
        for i in `seq 0 $last`; do
            port=$(($firstPort+$i))
            proxyPort=$(($firstProxyPort+$i))
            ./runAllQueries.sh $strategy $queriesFile "${l[i]}" $port $ldfServer ${federationFile} $availability $answersFolder $publicEndpoint $engine $configFile $hdtFile $anapsidFederationFile ${updatesFile}${port} ${proxyPort} ${action} > ${setupFolder}/output${label}${numberClients}Client$port & 
            pid=$!
            spids="$spids $pid"
        done
        for e in $spids; do
            wait $e
        done
        if [ "$strategy" = "FEDERATION" ] || [ "$strategy" = "PUBLIC" ]; then                                                                                                       
          if [ "$action" != "justReplicate" ]; then
            kill $pidPEProxy
            sleep 1
            cat $tmpFilePEP >> ${setupFolder}/output${label}${numberClients}ClientPublicEndpoint
          fi
        fi
        if [ "$strategy" = "FEDERATION" ]; then
          for i in `seq 0 $last`; do
            port=$(($firstPort+$i))
            file=${setupFolder}/output${label}${numberClients}Client$port
            n=`wc -l $file | sed 's/^[ ^t]*//' | cut -d' ' -f1`
            pidProxy=`head -n $n $file | tail -n 1`
            n=$(($n-1))
            pidFE=`head -n $n $file | tail -n 1`
            n=$(($n-1))
            tmpFileNR=`head -n $n $file | tail -n 1`
            if [ "$action" != "justReplicate" ]; then
                kill $pidProxy
                sleep 1
                cat $tmpFileNR >> $file
                rm $tmpFileNR
            fi
            if [ "$action" = "all" ]; then
                kill $pidFE
                rm ${setupFolder}/fedraFiles/views/view*
                cp ${setupFolder}/fedraFilesBkp/* ${setupFolder}/fedraFiles/
                cp ${setupFolder}/federation.ttl.bkp ${setupFolder}/federation.ttl
                cp ${setupFolder}/endpointsDescription.bkp ${setupFolder}/endpointsDescription
            fi
            if [ "$action" != "justExecute" ]; then
                updateTime=`./processUpdateFile.sh ${updatesFile}${port}`
                (echo "$updateTime") >> $file
                rm ${updatesFile}${port}
            fi
          done
          if [ "$action" = "justReplicate" ]; then
              dawIndexGenerationTime=`./generateFederationDawIndex.sh $setupFolder $firstPort $lastPort $firstProxyPort`
              echo "$dawIndexGenerationTime"
          fi
        fi
      done
    done
done
