LILAC (sparqL query decomposItion against federations of repLicAted data sourCes)
================================================================================

This folder includes the scripts used to execute the experiments included in the paper:

Gabriela Montoya, Hala Skaf-Molli, Pascal Molli, Maria-Esther Vidal. Decomposing Federated Queries
in presence of Replicated Fragments. Web Semantics: Science, Services and Agents on the World Wide
Web, Elsevier, 2017.


Requirements:
------------
* ANAPSID query engine, used version from May 2014, available at: https://github.com/anapsid/anapsid
* FedX query engine, used version 3.1, available at: https://www.fluidops.com/downloads/collateral/FedX%203.1.zip
* Jena, used version 2.11
* Virtuoso Open Source 7.2.1, available at https://github.com/openlink/virtuoso- opensource/
releases/tag/v7.2.1
* Apache HttpComponents Client library, used version 4.3.5, available at https://hc.apache.org/
* RDFLib python Library, available at https://github.com/RDFLib/rdflib
* sparqlwrapper, available at https://github.com/rdflib/sparqlwrapper
* Java HDT library, available at http://www.rdfhdt.org/manual-of-the-java-hdt-library/#download
* Fuseki, used version 1.1.1, available at http://jena.apache.org/documentation/serving\_data/#download-fuseki1
* HDT Fuseki, available at https://github.com/rdfhdt/hdt-java/tree/master/hdt-fuseki

Complete the file experimentsJoWS2017/setVariables.sh with the path to each of the requirements, and variable lilachome with the values of lilac folder absolute path.
To set the variables, execute: 
source ${lilachome}/experimentsJoWS2017/setVariables.sh

To compile the code, execute:
source ${lilachome}/scripts/compile.sh

Experiments were carried out using the Grid’5000 testbed, supported by a scientific interest group hosted by Inria and including CNRS, RENATER and several Universities as well as other organizations (see https: //www.grid5000.fr).

We relied on command "uniq $OAR\_NODEFILE" that returns the list of machines available for the process. 
To execute the code in other environment, delete lines 8-10 in file scripts/execute0.sh, and include file ${lilachome}/data/${federation}Setup/hosts with the addresses of the machines available for the experiment, one per line.
We also relied on command the "oarsh" to connect to the different machines in the setup to launch the endpoints and proxies, and the client. To execute the code in other environment, replace "oarsh" by the appropriated command at the files:
${lilachome}/experimentsJoWS2017/endFederation.sh
${lilachome}/experimentsJoWS2017/execute0.sh
${lilachome}/experimentsJoWS2017/runAllClients.sh
${lilachome}/experimentsJoWS2017/startFederation.sh


* Use the HDT library to uncompress the hdt files ${lilachome}/data/${federation}Setup/endpointX.hdt in each federation folder at $lilachome/data/


* Include FedX files:
```
mv $lilachome/engines/FedXFiles/BoundJoinConversionIteration.java $fedXPath/src/com/fluidops/fedx/evaluation/iterator/
mv $lilachome/engines/FedXFiles/CheckStatementPattern.java $fedXPath/src/com/fluidops/fedx/algebra/
mv $lilachome/engines/FedXFiles/ControlledWorkerBoundJoin.java $fedXPath/src/com/fluidops/fedx/evaluation/join/
mv $lilachome/engines/FedXFiles/ExclusiveGroup.java $fedXPath/src/com/fluidops/fedx/algebra/
mv $lilachome/engines/FedXFiles/QueryStringUtil.java $fedXPath/src/com/fluidops/fedx/util/
mv $lilachome/engines/FedXFiles/SkipStatementPattern.java $fedXPath/src/com/fluidops/fedx/algebra/
mv $lilachome/engines/FedXFiles/SparqlFederationEvalStrategy.java $fedXPath/src/com/fluidops/fedx/evaluation/
mv $lilachome/engines/FedXFiles/SparqlFederationEvalStrategyWithValues.java $fedXPath/src/com/fluidops/fedx/evaluation/
mv $lilachome/engines/FedXFiles/SynchronousBoundJoin.java $fedXPath/src/com/fluidops/fedx/evaluation/join/
mv $lilachome/engines/FedXFiles/*.java $fedXPath/src/com/fluidops/fedx/optimizer/
mv $lilachome/engines/FedXFiles/build.xml $fedXPath/
cd $fedXPath
ant jar
```

* Include ANAPSID files:
```
mv $lilachome/engines/AnapsidFiles/*.py $anapsidPath/ANAPSID/Decomposer/
mv $lilachome/engines/AnapsidFiles/run_anapsid $anapsidPath/scripts/
```
Experiments reported in the paper can be reproduced with the script: ${lilachome}/experimentsJoWS2017/executeAll.sh, after the data have been loaded in the Virtoso endpoints using the script: scripts/loadDataVirtuosoEndpoints.sh.

Note the GeoCoordinates federation (replace HOST by the machine address of one the available machine):
A virtuoso endpoint that provides access to all the accessible fragments in the federation should be deployed using the script: ${lilachome}/scripts/loadDataAllVirtuosoEndpoints.sh
This is required to correctly compute the soundness and correctness of the obtained answers as the answers that include real numbers are returned with different precision by Virtuoso and Fuseki endpoints.
This endpoint should be launched using the script: ${lilachome}/scripts/startOneEndpoint.sh geoCoordinates All HOST
The answers produced using the script: ${lilachome}/scripts/produceAnswersVirtuoso.sh ${lilachome}/data/geoCoordinatesSetup/queriesToExecute ${lilac home}/data/geoCoordinatesSetup/queries HOST 8890 ${lilachome}/data/geoCoordinatesSetup/answersVirtuoso/
After the answers have been produced, the endpoint execution can be ended.
