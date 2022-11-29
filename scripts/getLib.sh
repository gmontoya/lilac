cd $httpcomponentsClientPath

wget https://repo1.maven.org/maven2/org/apache/httpcomponents/httpcore/4.3.2/httpcore-4.3.2.jar
wget https://repo1.maven.org/maven2/org/apache/httpcomponents/httpclient/4.3.5/httpclient-4.3.5.jar
wget https://repo1.maven.org/maven2/commons-logging/commons-logging/1.2/commons-logging-1.2.jar
wget https://archive.apache.org/dist/jena/binaries/apache-jena-2.11.0.tar.gz

gunzip apache-jena-2.11.0.tar.gz
tar -xvf apache-jena-2.11.0.tar

wget http://archive.apache.org/dist/jena/binaries/apache-jena-fuseki-2.6.0.tar.gz
gunzip apache-jena-fuseki-2.6.0.tar.gz
tar xvf apache-jena-fuseki-2.6.0.tar

