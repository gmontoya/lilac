lilachome=$lilachome
fedXPath=$fedXPath
anapsidPath=$anapsidPath

mv $fedXPath/lib/fedx-3.1.jar $fedXPath/

cp $lilachome/engines/FedXFiles/BoundJoinConversionIteration.java $fedXPath/src/com/fluidops/fedx/evaluation/iterator/
cp $lilachome/engines/FedXFiles/CheckStatementPattern.java $fedXPath/src/com/fluidops/fedx/algebra/
cp $lilachome/engines/FedXFiles/ControlledWorkerBoundJoin.java $fedXPath/src/com/fluidops/fedx/evaluation/join/
cp $lilachome/engines/FedXFiles/ExclusiveGroup.java $fedXPath/src/com/fluidops/fedx/algebra/
cp $lilachome/engines/FedXFiles/QueryStringUtil.java $fedXPath/src/com/fluidops/fedx/util/
cp $lilachome/engines/FedXFiles/SkipStatementPattern.java $fedXPath/src/com/fluidops/fedx/algebra/
cp $lilachome/engines/FedXFiles/SparqlFederationEvalStrategy.java $fedXPath/src/com/fluidops/fedx/evaluation/
cp $lilachome/engines/FedXFiles/SparqlFederationEvalStrategyWithValues.java $fedXPath/src/com/fluidops/fedx/evaluation/
cp $lilachome/engines/FedXFiles/SynchronousBoundJoin.java $fedXPath/src/com/fluidops/fedx/evaluation/join/

files="AddStatementSourceVisitor.java BGPVisitor.java Capability.java DawSourceSelection.java EndpointComparator.java EndpointSummary.java FedraSourceSelection.java JoinOrderOptimizer.java LilacDecomposer.java Optimizer.java Pair.java PairComparator.java SourceSelection.java StatementGroupOptimizer.java StatementPatternComparator.java StatementSourceComparator.java StatementsVisitor.java TriplePatternFragment.java"
for f in ${files}; do
    cp $lilachome/engines/FedXFiles/${f} $fedXPath/src/com/fluidops/fedx/optimizer/
done

cp $lilachome/engines/FedXFiles/build.xml $fedXPath/
cd $fedXPath
ant jar

chmod 755 cli.sh

git clone https://github.com/anapsid/anapsid.git

cp $lilachome/engines/AnapsidFiles/*.py $anapsidPath/ANAPSID/Decomposer/
cp $lilachome/engines/AnapsidFiles/run_anapsid $anapsidPath/scripts/


cd $anapsidPath

echo "ply==3.4" > requirements.txt
echo "requests" >> requirements.txt
echo "sparqlwrapper" >> requirements.txt
echo "rdflib" >> requirements.txt

pip install -r requirements.txt

