LILAC (sparqL query decomposItion against federations of repLicAted data sourCes)
================================================================================

Decomposing Federated Queries in presence of Replicated Fragments

Scripts used to run the experiments reported in the article "Decomposing Federated Queries in presence of Replicated Fragments" (Journal of Web Semantics, 2017) are available in the folder `experimentsJoWS2017`

In this repository, the code of the extensions to the engines ANAPSID and FedX are included. 
These extensions implement query optimization strategies DAW, Fedra, and LILAC. 
ANAPSID's code is licensed under GNU/GPL v2 and FedX's code is licensed under the GNU Affero General Public License. 
The code of these engines was obtained from:

* https://github.com/anapsid/anapsid
* https://www.fluidops.com/downloads/collateral/FedX%203.1.zip

The script `enhanceEngines.sh` was used to obtain these extensions (do not need to be re-executed)

Requirements
------------
* Java, used version java-1.11.0-openjdk-amd64
* Virtuoso Open Source 7.2 Docker Image, available at https://hub.docker.com/r/openlink/virtuoso-opensource-7
* Daw Index Generator, available at https://github.com/momo54/FedraDawIndex (only required to generate new DAW indexes)

Libraries
---------
* Jena, used version 2.11.0, available at https://archive.apache.org/dist/jena/binaries/apache-jena-2.11.0.tar.gz
* Java HDT library, used version v3.0.5, https://github.com/rdfhdt/hdt-java.git
* Apache Jena Fuseki, used version 2.6.0, http://archive.apache.org/dist/jena/binaries/apache-jena-fuseki-2.6.0.tar.gz
* Apache HttpComponents Client library, used version 4.3.5, https://repo1.maven.org/maven2/org/apache/httpcomponents/httpcore/4.3.2/httpcore-4.3.2.jar, https://repo1.maven.org/maven2/org/apache/httpcomponents/httpclient/4.3.5/httpclient-4.3.5.jar, https://repo1.maven.org/maven2/commons-logging/commons-logging/1.2/commons-logging-1.2.jar

Obtaining Libraries
-------------------

* Set the variable `JAVA_HOME`, for example:

`$ export JAVA_HOME=/usr/lib/jvm/java-1.11.0-openjdk-amd64`

* Download the libraries, if not already in the folder `lib`

`$ source getLib.sh`

* Obtain Java HDT Fuseki (initially, Fuseki 1 was used but due to incompatibility with later versions of Java, an update to Fuseki 2 was seeked and following the recommendations at https://github.com/rdfhdt/hdt-java/tree/master/hdt-jena the script setUpFusekiHDT.sh has been written to take care of the changes needed)

`$ source setUpFusekiHDT.sh`

Variables
---------

Complete the file `scripts/variables` with the path to each of the requirements, and variable `lilachome` with the absolute path of this repository,
`dockerEndpointFolder` with the absolute path of a folder that can be used to store the endpoint files (within this folder, a folder will be created by the
script `startDockers.sh` for each of the endpoints)

* To set the variables, execute:

`$ cd scripts`

`$ source replaceVariablesByPaths.sh`

Compilation
-----------

* To compile the code, execute:

`$ cd ${lilachome}/scripts`

`$ source compile.sh`


Endpoints
---------

* Virtuoso endpoints can be set up using the scripts:

`# creates the dockers for all the federations`

`$ ./createDockers.sh`

`# uncompress the hdt files ${lilachome}/data/${federation}Setup/endpointX.hdt in each federation folder at $lilachome/data/`

`$ ./uncompressData.sh`

`# creates isql files to load the .nt files available for each federation endpoint $lilachome/data/${federation}Setup`

`$ ./createLoadFiles.sh`

`# loads the data into each federation endpoint, relies on the dockers and files created by the previous two scripts`

`$ ./loadDataDockers.sh`

* Endpoints have been set up in ports 8890-8899 (for a federation with 10 endpoints)

* (Shallow) Check that the endpoints have been correctly set:

`# start the federation`

`$ ./restartDockers.sh diseasome`

`$ cd $lilachome/lib/apache-jena-2.11.0/bin`

`# get the predicates of the triples available through endpoint 8890`

`$ ./rsparql --service=http://localhost:8890/sparql "SELECT DISTINCT ?p WHERE { ?s ?p ?o }"`

`# get the number of the triples available through endpoint 8890`

`$ ./rsparql --service=http://localhost:8890/sparql "SELECT (COUNT(*) AS ?c) WHERE { ?s ?p ?o }"`

`# get a sample of the triples available through endpoint 8890`

`$ ./rsparql --service=http://localhost:8890/sparql "SELECT * WHERE { ?s ?p ?o } LIMIT 10"`

`# stop the federation`

`$ ./stopDockers.sh diseasome`

Experiments
-----------

`$ cd $lilachome/scripts`

* Save the current state of the federations

`$ ./save.sh`

* Use Virtuoso endpoints

`$ ./useVirtuosoEndpoints.sh`

* Check or update the parameters of the experiment, e.g., for the diseasome federation

`$ vim testWithIndividualMeasures.sh`
`$ vim $lilachome/data/diseasomeSetup/confFile`

* Execute experiments with one federation,e.g., for the diseasome federation

`$ ./execute.sh diseasome Diseasome`


Executing one query
-------------------

To execute one query, you can rely on the existing commands from FedX and ANAPSID

* For FedX and LILAC/Fedra

`$ cd $fedXPath`

`$ ./cli.sh -c configFile -d federationDescriptionFile -f JSON -folder results @q queryStringFile`

where configFile corresponds to a configuration file such as $lilachome/data/diseasomeSetup/confFile with values at least for the parameters: EndpointsFile, FragmentsSources, FragmentsDefinitionFolder, PredicateIndex, SourceSelectionStrategy, Random

federationDescriptionFile is a FedX federation description and queryStringFile is a text file that contains the query to execute

About the parameters: 

EndpointsFile - a text file with a line for each fragment that states the endpoints in the federation that have replicated the fragment

An example for the diseasome federation can be found at $lilachome/data/diseasomeSetup/fedraFiles/endpoints

FragmentsSources - a text file with a line for each fragment that state the public endpoint from where it was replicated

An example for the diseasome federation can be found at $lilachome/data/diseasomeSetup/fedraFiles/fragmentsSources

FragmentsDefinitionFolder - a folder that contains one file for each fragment with its description as a CONSTRUCT query

An example for the diseasome federation can be found at $lilachome/data/diseasomeSetup/fedraFiles/fragments

PredicateIndex - a text file with a line for each predicate that states the fragments replicated in the federation that include triples with that predicate

An example for the diseasome federation can be found at $lilachome/data/diseasomeSetup/fedraFiles/predicateIndex

SourceSelectionStrategy - a string with the source selection strategy to use. Supported values: LILAC, Fedra, DAW, engine. engine sets the source selection to FedX's

Random - a boolean value in { true, false } using true allows to possibly select different endpoints to evaluate triple patterns, when multiple candidate endpoints seem to provide the same benefits (e.g., have triples with the same number of relevant predicates

* For ANAPSID and LILAC/Fedra

`$ cd $anapsidPath/scripts`

`$ run_anapsid -e anapsidFederationDescriptionFile -q queryStringFile -c configFile -s False -p decomposition -o False -d SSGM -a True -w False -r True -f queryAnswer -k b

where configFile corresponds to a configuration file such as $lilachome/data/diseasomeSetup/confFile with values at least for the parameters: EndpointsFile, FragmentsSources, FragmentsDefinitionFolder, PredicateIndex, SourceSelectionStrategy, Random ( as described above )

anapsidFederationDescriptionFile is an anapsid federation description, queryStringFile is a text file that contains the query to execute, decomposition is either 'd' or 'b', 'd' states that source selection and decomposition is required without the execution and 'b' states for query execution, queryAnswer is the location where the query answer is stored


