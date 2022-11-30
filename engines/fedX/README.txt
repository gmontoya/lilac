Readme

First steps and important information:

 * Documentation can be found in doc-folder
 * Required libraries are in the lib folder
 * Mailing List: iwb-discussion (at) googlegroups.com
 * Contact: Andreas.Schwarte (at) fluidops.com
 * Project Website: http://www.fluidops.com/FedX
 * FedX is licensed under AGPL (see attached license). 

(c) 2011-2014 fluid Operations AG
http://www.fluidops.com
Last updated: 07.10.2014

---------------
The files in this folder corresponds to the files provided as FedX3.1.zip (as it was available at https://www.fluidops.com/downloads/collateral/FedX%203.1.zip) with the modifications done to implement query optimization strategies DAW, Fedra, and LILAC
- src/com/fluidops/fedx/algebra/CheckStatementPattern.java, class modified 
- src/com/fluidops/fedx/algebra/ExclusiveGroup.java, class modified 
- src/com/fluidops/fedx/algebra/SkipStatementPattern.java, class added 
- src/com/fluidops/fedx/evaluation/SparqlFederationEvalStrategy.java, class modified 
- src/com/fluidops/fedx/evaluation/SparqlFederationEvalStrategyWithValues.java, class modified 
- src/com/fluidops/fedx/evaluation/iterator/BoundJoinConversionIteration.java, class modified
- src/com/fluidops/fedx/evaluation/join/ControlledWorkerBoundJoin.java, class modified
- src/com/fluidops/fedx/evaluation/join/SynchronousBoundJoin.java, class modified
- src/com/fluidops/fedx/optimizer/AddStatementSourceVisitor.java, class added 
- src/com/fluidops/fedx/optimizer/BGPVisitor.java, class added 
- src/com/fluidops/fedx/optimizer/Capability.java, class added 
- src/com/fluidops/fedx/optimizer/DawSourceSelection.java, class added 
- src/com/fluidops/fedx/optimizer/EndpointComparator.java, class added 
- src/com/fluidops/fedx/optimizer/EndpointSummary.java, class added 
- src/com/fluidops/fedx/optimizer/FedraSourceSelection.java, class added 
- src/com/fluidops/fedx/optimizer/JoinOrderOptimizer.java, class modified
- src/com/fluidops/fedx/optimizer/LilacDecomposer.java, class added 
- src/com/fluidops/fedx/optimizer/Optimizer.java, class modified
- src/com/fluidops/fedx/optimizer/Pair.java, class added 
- src/com/fluidops/fedx/optimizer/PairComparator.java, class added 
- src/com/fluidops/fedx/optimizer/SourceSelection.java, class modified
- src/com/fluidops/fedx/optimizer/StatementGroupOptimizer.java, class modified
- src/com/fluidops/fedx/optimizer/StatementPatternComparator.java, class added 
- src/com/fluidops/fedx/optimizer/StatementSourceComparator.java, class added 
- src/com/fluidops/fedx/optimizer/StatementsVisitor.java, class added 
- src/com/fluidops/fedx/optimizer/TriplePatternFragment.java, class added 
- src/com/fluidops/fedx/util/QueryStringUtil.java, class modified
- build.xml, file added
- examples/testFederation.ttl
- examples/testFederationProxies.ttl

