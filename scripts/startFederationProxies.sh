firstPort=8890
lastPort=8899
firstProxyPort=3130
pePort=8900
peProxyPort=3100
proxyFolder=$lilachome/proxy
peGraph=""
graphIndex=0
federation=$1
graphPrefix=http://${federation}Endpoint

./startProxies.sh $firstPort $lastPort $firstProxyPort $pePort $peProxyPort $proxyFolder $federation "$peGraph" $graphPrefix $graphIndex

