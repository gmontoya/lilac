firstPort=8890
lastPort=8899
firstProxyPort=3130
pePort=8890
peProxyPort=3100
proxyFolder=${fedrahome}/proxy
peGraph=""
graphPrefix=""
graphIndex=""
federation=$1

./startProxies.sh $firstPort $lastPort $firstProxyPort $pePort $peProxyPort $proxyFolder $federation "$peGraph" "$graphPrefix" "$graphIndex"

