firstPort=3030
lastPort=3039
firstProxyPort=3130
pePort=8890
peProxyPort=3100
proxyFolder=$fedrahome/proxy
peGraph=""
graphPrefix=""
graphIndex=""

./startProxies.sh $firstPort $lastPort $firstProxyPort $pePort $peProxyPort $proxyFolder "$peGraph" "$graphPrefix" "$graphIndex"

