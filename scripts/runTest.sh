#!/bin/bash

federation=$1
Federation=$2

setupFolder=$lilachome/data/${federation}Setup
confFile=$setupFolder/confFile

strategy="$3"
engines="$4"
sourceSelectionStrategy="$5"
action="$6"

#uses the values given in testWithIndividualMeasures
numberClients=$7
numberQueries=$8
port=$9

sed -i "s,numClients=.*,numClients=${numberClients}," ${confFile}
sed -i "s,numQueries=.*,numQueries=${numberQueries}," ${confFile}
sed -i "s,firstPort=.*,firstPort=${port}," ${confFile}

source ${confFile}

#host=`./getHost.sh $setupFolder/hosts ${peProxyPort} ${firstProxyPort}`
host=localhost
host=http://$host
if [ "$action" = "justSelect" ]; then
  publicEndpoint=${host}:${pePort}/sparql
else
  publicEndpoint=${host}:${peProxyPort}/sparql
fi

ldfServer=${host}:5000/${federation}

./runAllClients.sh "${strategy}" ${numClients} ${queriesFile} ${numQueries} ${firstPort} ${ldfServer} ${federationFile} ${availability} ${answersFolder} ${publicEndpoint} "${engines}" ${confFile} ${endpointsDescription} ${updatesFile} ${firstProxyPort} "${sourceSelectionStrategy}" $action "$queriesToExecute" $pePort $peProxyPort $setupFolder ${federation}
