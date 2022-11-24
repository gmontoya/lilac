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
federation=${23}
tmpFilePEP=`mktemp`
lastPort=$(($firstPort+$numberClients-1))
last=$(($numberClients-1))
tmpFile=`mktemp`

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
            ${lilachome}/scripts/produceAnswer.sh "${l[i]}" "$queriesFile" "$hdtFile" "${firstProxyPort}" "$answersFolder"
        fi
    fi
done

if [ "$action" = "all" ] && [[ "$strategies" == *"FEDERATION"* ]]; then
    rm -fr ${setupFolder}/fedraFilesBkp
    mkdir ${setupFolder}/fedraFilesBkp
    cp -r ${setupFolder}/fedraFiles/* ${setupFolder}/fedraFilesBkp/
    cp ${setupFolder}/federation.ttl ${setupFolder}/federation.ttl.bkp
    cp ${setupFolder}/endpointsDescription ${setupFolder}/endpointsDescription.bkp
fi

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
          if [ "$action" != "justReplicate" ] && [ "$action" != "justSelect" ]; then
            p=`pwd`
            address=`${lilachome}/scripts/getHost.sh $setupFolder/hosts 3040`
            cd ${lilachome}/proxy
            java -cp .:${httpcomponentsClientPath}/lib/* SingleEndpointProxy2 $address ${publicEndpointPort} ${publicEndpointProxyPort} > $tmpFilePEP &
            pidPEProxy=$!
            cd $p
          fi
        fi
        sed -i".bkp" "s/SourceSelectionStrategy=[a-zA-Z_]*/SourceSelectionStrategy=${ss}/" ${configFile}
        label="$engine${ss}${strategy}"
        for i in `seq 0 $last`; do
            port=$(($firstPort+$i))
            proxyPort=$(($firstProxyPort+$i))
            p=`pwd`
            address=`${lilachome}/scripts/getHost.sh $setupFolder/hosts $port`
            host=http://$address
            cd $p
            graph=http://${federation}Endpoint${i}
            oarsh $address "${lilachome}/scripts/runClient.sh $strategy $queriesFile \"${l[i]}\" $port $ldfServer ${federationFile} $availability $answersFolder $publicEndpoint $engine $configFile $hdtFile $anapsidFederationFile ${updatesFile}${port} ${proxyPort} ${action} ${address} ${setupFolder}/output${label}${numberClients}Client$port ${graph} > ${tmpFile}" 
            pid=`cat $tmpFile`
            spids="$spids $pid"
        done
        echo "clients started with pids $spids"
        while true; do
            ready=0
            for i in `seq 0 $last`; do
                port=$(($firstPort+$i))
                output=${setupFolder}/output${label}${numberClients}Client$port
                line=`tail -n 1 $output`
                if [ "$line" = "END" ]; then
                    ready=$(($ready+1))
                fi
            done
            if [ "$ready" = "$numberClients" ]; then
                break
            else
                sleep 10s
            fi
        done
        echo "clients finished"
        #sleep 1d
        if [ "$strategy" = "FEDERATION" ] || [ "$strategy" = "PUBLIC" ]; then                                                                                                       
          if [ "$action" != "justReplicate" ] && [ "$action" != "justSelect" ]; then
            echo "pidPEProxy: $pidPEProxy"
            kill $pidPEProxy
            echo "pidPEProxy killed"
            sleep 1
            cat $tmpFilePEP >> ${setupFolder}/output${label}${numberClients}ClientPublicEndpoint
          fi
        fi
        if [ "$strategy" = "FEDERATION" ]; then
          for i in `seq 0 $last`; do
            port=$(($firstPort+$i))
            file=${setupFolder}/output${label}${numberClients}Client$port
            n=`wc -l $file | sed 's/^[ ^t]*//' | cut -d' ' -f1`
            n=$(($n-1))
            pidProxy=`head -n $n $file | tail -n 1`
            n=$(($n-1))
            pidFE=`head -n $n $file | tail -n 1`
            n=$(($n-1))
            tmpFileNR=`head -n $n $file | tail -n 1`
            proxyAddress=`${lilachome}/scripts/getHost.sh $setupFolder/hosts $port`
            if [ "$action" != "justReplicate" ] && [ "$action" != "justSelect" ]; then
                #echo "killing proxy with pid: $pidProxy in machine $proxyAddress"
                oarsh $proxyAddress "${lilachome}/scripts/endProxy.sh $pidProxy"
                sleep 2s
                #cat $tmpFileNR  
                #echo "end of tmpFileNR: $tmpFileNR"              
                cat $tmpFileNR >> $file
            fi
            if [ "$action" = "all" ]; then
                oarsh $proxyAddress "${lilachome}/scripts/endProxy.sh $pidFE"
                rm ${setupFolder}/fedraFiles/views/view*
                cp -r ${setupFolder}/fedraFilesBkp/* ${setupFolder}/fedraFiles/
                cp ${setupFolder}/federation.ttl.bkp ${setupFolder}/federation.ttl
                cp ${setupFolder}/endpointsDescription.bkp ${setupFolder}/endpointsDescription
            fi
            if [ "$action" != "justExecute" ] && [ "$action" != "justSelect" ]; then
                updateTime=`${lilachome}/scripts/processUpdateFile.sh ${updatesFile}${port}`
                (echo "$updateTime") >> $file
                rm ${updatesFile}${port}
            fi
          done
          if [ "$action" = "justReplicate" ]; then
              dawIndexGenerationTime=`${lilachome}/scripts/generateFederationDawIndex.sh $setupFolder $firstPort $lastPort $firstProxyPort`
              echo "$dawIndexGenerationTime"
          fi
        fi
        #sleep 1d
      done
    done
done
