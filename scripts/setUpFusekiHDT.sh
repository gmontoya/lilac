# there is no readily available fuseki that works well with the latest hdt-java
# see issue here:
# https://github.com/rdfhdt/hdt-java/issues/100
# one way around is to use hdt-jena and fuseki 2 as suggested here:
# https://github.com/rdfhdt/hdt-java/tree/master/hdt-jena
# the solution proposed there was adapted to work well with fuseki 2.6.0 and hdt-java 3.0.5


cd $httpcomponentsClientPath

# version used 3.0.5, uses Java 11
git clone https://github.com/rdfhdt/hdt-java.git

cd $hdtJavaPath
mvn install

cd hdt-jena
mvn install

cd ../hdt-api
mvn install

cd ../hdt-java-core

mvn package dependency:copy-dependencies

hdtapi=`ls $hdtJavaPath/hdt-api/target/hdt-api-*.jar | grep -v "sources" | grep -v "javadoc"`
hdtjavacorecompressdependency=`ls $hdtJavaPath/hdt-java-core/target/dependency/commons-compress*.jar | grep -v "sources" | grep -v "javadoc"`
hdtjavacorearraysdependency=`ls $hdtJavaPath/hdt-java-core/target/dependency/JLargeArrays*.jar | grep -v "sources" | grep -v "javadoc"`
hdtjavacoremathdependency=`ls $hdtJavaPath/hdt-java-core/target/dependency/commons-math*.jar | grep -v "sources" | grep -v "javadoc"`
hdtjavacore=`ls $hdtJavaPath/hdt-java-core/target/hdt-java-core* | grep -v "sources" | grep -v "javadoc"`
hdtjena=`ls $hdtJavaPath/hdt-jena/target/hdt-jena* | grep -v "sources" |  grep -v "javadoc"`

cp="${hdtapi}:${hdtjavacorecompressdependency}:${hdtjavacorearraysdependency}:${hdtjavacoremathdependency}:${hdtjavacore}:${hdtjena}:\$JAR"


cd $fusekiPath
sed -i".bkp" "s,exec .*,exec \$JAVA \$JVM_ARGS -cp \"${cp}\" org.apache.jena.fuseki.cmd.FusekiCmd \"\$\@\",g" fuseki-server

