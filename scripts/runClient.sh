strategy=$1
queriesFile=$2
queries="$3"
port=$4
ldfServer=$5
federationFile=$6
availability=$7
answersFolder=$8
publicEndpoint=$9
engine=${10}
configFile=${11}
hdtFile=${12}
anapsidFederationFile=${13}
updatesFile=${14}
proxyPort=${15}
action=${16}
address=${17}
output=${18}

${fedrahome}/scripts/runAllQueries.sh $strategy $queriesFile "${queries}" $port $ldfServer ${federationFile} $availability $answersFolder $publicEndpoint $engine $configFile $hdtFile $anapsidFederationFile ${updatesFile} ${proxyPort} ${action} ${address} > ${output} &
pid=$!

echo $pid
