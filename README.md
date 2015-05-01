fedra
=====

Federated SPARQL Queries Processing with Replicated Fragments

Requirements:
------------
* ANAPSID query engine, used version from May 2014, available at: https://github.com/anapsid/anapsid
* FedX query engine, used version 3.0, available at: http://www.fluidops.com/downloads/collateral/FedX%203.0.zip
* Jena, used version 2.11
* Waterloo SPARQL Diversity Test Suite (WatDiv), used version v0.5 available at: http://db.uwaterloo.ca/watdiv/
* Fuseki, used version 1.1.1, available at http://jena.apache.org/documentation/serving\_data/#download-fuseki1
* Daw index generator, available at https://github.com/momo54/FedraDawIndex
* Java HDT library, available at http://www.rdfhdt.org/manual-of-the-java-hdt-library/#download
* HDT Fuseki, available at https://github.com/rdfhdt/hdt-java/tree/master/hdt-fuseki
* Apache HttpComponents Client library, used version 4.3.5, available at https://hc.apache.org/
* RDFLib python Library, available at https://github.com/RDFLib/rdflib
* sparqlwrapper, available at https://github.com/rdflib/sparqlwrapper

Complete the file scripts/setVariables.sh with the path to each of the requirements, and variables fedrahome and host with the values of fedra folder absolute path and the address of the machine to host the endpoints (including 'http://').

* Use the HDT library to uncompress the hdt files endpointX.nt in each federation folder at $fedrahome/data/

* Include FedX files:
$ mv $fedrahome/engines/FedXFiles/\*.java $fedXPath/src/com/fluidops/fedx/optimizer/
$ mv $fedrahome/engines/build.xml $fedXPath/
$ cd $fedXPath
$ ant jar

* Include ANAPSID files:
$ mv $fedrahome/engines/AnapsidFiles/\*.py $anapsidPath/ANAPSID/Decomposer/
$ mv $fedrahome/engines/AnapsidFiles/run\_anapsid $anapsidPath/scripts/

Experiments reported at https://sites.google.com/site/fedrasourceselection can be reproduced using $fedrahome/scripts/script.sh

