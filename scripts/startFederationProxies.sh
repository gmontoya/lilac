firstPort=3030
lastPort=3039
firstProxyPort=3130
pePort=8890
peProxyPort=3100
proxyFolder=$fedrahome/proxy
address=${host#http://}
peGraph=""
graphPrefix=""
graphIndex=""

./startProxies.sh $firstPort $lastPort $firstProxyPort $pePort $peProxyPort $proxyFolder "$address" "$peGraph" "$graphPrefix" "$graphIndex"

