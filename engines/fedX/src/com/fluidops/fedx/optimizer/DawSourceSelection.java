package com.fluidops.fedx.optimizer;

import java.util.*;
import java.io.*;

import org.openrdf.query.parser.QueryParser;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.rio.Rio;
import org.openrdf.model.Statement;
import org.openrdf.model.Model;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.ValueFactory;
import org.openrdf.rio.RDFFormat;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.Repository;
import org.openrdf.sail.memory.MemoryStore;

import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.queryrender.sparql.SparqlTupleExprRenderer;

import com.fluidops.fedx.algebra.StatementSource;
import com.fluidops.fedx.structures.Endpoint;
import com.fluidops.fedx.Config;

/*
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.Element;*/
//import com.hp.hpl.jena.rdf.model.regression.Regression.LitTestObjF;
//import com.hp.hpl.jena.rdf.common.ResourceImpl;

class DawSourceSelection {

    private Map<StatementPattern, List<StatementSource>> stmtToSources;
    private Map<String,String> endpoints; // for each endpointId, its endpointURL
    private HashMap<String, EndpointSummary> summaries;
    private long threshold;

    public DawSourceSelection(Map<StatementPattern, List<StatementSource>> stmtToSources, List<Endpoint> endpoints) {

        this.stmtToSources = stmtToSources;
        this.endpoints = obtainEndpointMap(endpoints);
        //System.out.println("Starting the index loading");
        loadEndpointSummaries(Config.getConfig().getProperty("DawIndex"));
        //System.out.println("Index loaded"+summaries.size()+" elements loaded.");
        this.threshold = Long.parseLong(Config.getConfig().getProperty("Threshold"));
    }

    public static Map<String,String> obtainEndpointMap(List<Endpoint> endpoints) {

        Map<String,String> map = new HashMap<String,String>();
        for (Endpoint e : endpoints) {
            map.put(e.getId(), e.getEndpoint());
        }
        return map;
    }

    public void loadEndpointSummaries(String indexFile) {

	this.summaries =  new HashMap<String, EndpointSummary>();
        try {
            Repository repo = new SailRepository( new MemoryStore());
            repo.initialize();
            RepositoryConnection con = repo.getConnection();
            File file = new File(indexFile);
            RDFFormat format = Rio.getParserFormatForFileName(indexFile);
            con.add(file, "http://dummy", format);

            String queryStr = "PREFIX sd: <http://www.w3.org/ns/sparql-service-description#> \n ";
            queryStr += "SELECT DISTINCT ?u ?pred ?tt ?ass ?aos ?mips \n WHERE { ?s a sd:Service . ?s sd:endpointUrl ?u . ?s sd:capability ?c . \n";
            queryStr += "?c <http://www.w3.org/ns/sparql-service-description#predicate> ?pred . \n";
            queryStr += "?c <http://www.w3.org/ns/sparql-service-description#totalTriples> ?tt . \n";
            queryStr += "?c <http://www.w3.org/ns/sparql-service-description#avgSbjSel> ?ass . \n";
            queryStr += "?c <http://www.w3.org/ns/sparql-service-description#avgObjSel> ?aos . \n";
            queryStr += "?c <http://www.w3.org/ns/sparql-service-description#MIPs> ?mips . }";
            TupleQuery tq = con.prepareTupleQuery(QueryLanguage.SPARQL, queryStr);
            TupleQueryResult tqr = tq.evaluate();
            while (tqr.hasNext()) {
                BindingSet bs = tqr.next();
                Value url = bs.getValue("u");
                String endpointAddress = url.stringValue();
                Value capa = bs.getValue("c");
                    String pred = bs.getValue("pred").stringValue();
                    //System.out.println("readed pred: "+pred);
                    long tt = Long.parseLong(bs.getValue("tt").stringValue());
                    double ass = Double.parseDouble(bs.getValue("ass").stringValue());
                    double aos = Double.parseDouble(bs.getValue("aos").stringValue());
                    String mips = bs.getValue("mips").stringValue();
                    Vector<Long> v = parseVector(mips);
                    Capability capability = new Capability(tt, ass, aos, v);
                    EndpointSummary endpointSummary = summaries.get(endpointAddress);
                    if (endpointSummary == null) {
                        endpointSummary = new EndpointSummary();
                    }   
                    endpointSummary.addCapability(pred, capability);
                    summaries.put(endpointAddress, endpointSummary);
            }
            tqr.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace(); 
        } finally {

        }
    }

    public void loadEndpointSummaries0(String indexFile) {

        this.summaries =  new HashMap<String, EndpointSummary>();
        try {
            RDFFormat format = Rio.getParserFormatForFileName(indexFile);
            FileInputStream fis = new FileInputStream(indexFile);
            Model results = Rio.parse(fis, "http://dummy", format);

            ValueFactory vf = ValueFactoryImpl.getInstance();
            URI type = vf.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
            URI serviceType = vf.createURI("http://www.w3.org/ns/sparql-service-description#Service");
            URI endpointURL = vf.createURI("http://www.w3.org/ns/sparql-service-description#endpointUrl");
            URI capabilityPred = vf.createURI("http://www.w3.org/ns/sparql-service-description#capability");
            URI predicate = vf.createURI("http://www.w3.org/ns/sparql-service-description#predicate");
            URI totalTriples = vf.createURI("http://www.w3.org/ns/sparql-service-description#totalTriples");
            URI avgSubSel = vf.createURI("http://www.w3.org/ns/sparql-service-description#avgSbjSel");
            URI avgObjSel = vf.createURI("http://www.w3.org/ns/sparql-service-description#avgObjSel");
            URI mips = vf.createURI("http://www.w3.org/ns/sparql-service-description#MIPs");

            Iterator<Statement> servicesIter = results.match(null, type, serviceType);
            while (servicesIter.hasNext()) {
                Statement serviceSt = servicesIter.next();
                Resource service = serviceSt.getSubject();
                Iterator<Statement> urlsIter = results.match(service, endpointURL, null);
                while (urlsIter.hasNext()) {
                    String endpointAddress = urlsIter.next().getObject().stringValue();
                    Iterator<Statement> capabilitiesIter = results.match(service, capabilityPred, null);
                    while (capabilitiesIter.hasNext()) {
                        Statement capabilitySt = capabilitiesIter.next();
                        Value capabilityVal = capabilitySt.getObject();
                        if (!(capabilityVal instanceof Resource)) {
                            continue;
                        }
                        Resource c = (Resource) capabilityVal;
                        Iterator<Statement> totalSizeIter = results.match(c, totalTriples, null);
                        long total = Long.parseLong(totalSizeIter.next().getObject().stringValue());
                        Iterator<Statement> predicateIter = results.match(c, predicate, null);
                        String pred = predicateIter.next().getObject().stringValue();
                        Iterator<Statement> avgSubSelIter = results.match(c, avgSubSel, null);
                        double subSel = Double.parseDouble(avgSubSelIter.next().getObject().stringValue());
                        Iterator<Statement> avgObjSelIter = results.match(c, avgObjSel, null);
                        double objSel = Double.parseDouble(avgObjSelIter.next().getObject().stringValue());
                        Iterator<Statement> mipsIter = results.match(c, mips, null);
                        String mipsStr = mipsIter.next().getObject().stringValue();
                        Vector<Long> v = parseVector(mipsStr);
                        Capability capability = new Capability(total, subSel, objSel, v);
                        EndpointSummary endpointSummary = summaries.get(endpointAddress);
                        if (endpointSummary == null) {
                            endpointSummary = new EndpointSummary();
                        }
                        endpointSummary.addCapability(pred, capability);
                        summaries.put(endpointAddress, endpointSummary);
                    }
                }
            }
        } catch (Exception e) {
            
        }
    }

    public Map<StatementPattern, List<StatementSource>> getSelectedSources() {

        return stmtToSources;
    }

    public void performSourceSelection () {

        for (StatementPattern sp : stmtToSources.keySet()) {
            List<StatementSource> capableDatasources = stmtToSources.get(sp);
            // Next statement is unnecessary to execute DAW once, but useful to avoid no deterministic behavior
            Collections.sort(capableDatasources, new StatementSourceComparator());
            //System.out.println("triple: "+sp);
            //System.out.println("the capable sources are: "+capableDatasources);
            List<StatementSource> bestSources = new ArrayList<StatementSource>();
            if (capableDatasources.size()>0) {
                bestSources = sourceWiseRankingAndSkipping(sp, capableDatasources);
            }
            stmtToSources.put(sp, bestSources);
        }
    }
    
    public static Vector<Long> parseVector(String s) {
        
        Vector<Long> v = new Vector<Long>();
        StringTokenizer st = new StringTokenizer(s, ",[] ");
        while (st.hasMoreTokens()) {
            
            String t = st.nextToken();
            Long i = new Long(Long.parseLong(t));
            v.add(i);
        }
        return v;
    }
    
    public StatementSource getMaxSizeSource(List<StatementSource> capableDatasources, StatementPattern sp) {

        StatementSource endpoint = null;
        double maxSize = 0;
        String p = sp.getPredicateVar().getValue().stringValue();
        //System.out.println("triple: "+sp+" number of capable sources: "+capableDatasources.size()); 
        for (StatementSource cd : capableDatasources) {
            String endpointId = cd.getEndpointID();
            String endpointUrl = this.endpoints.get(endpointId);
            EndpointSummary endpointSummary = this.summaries.get(endpointUrl);
            if (endpointSummary == null) { // Ignore endpoints that are not in the index
                //System.out.println("endpoint "+endpointId+" not in the index");
                continue;
            }
            Capability c = endpointSummary.getCapability(p);
            if (c == null) {  // Ignore predicates that are not in the index
                //System.out.println("predicate "+p+" not in the index of endpoint "+endpointId);
                continue;
            }
            double size = c.getTotal();
            if (sp.getSubjectVar().hasValue()) {
                size = size * c.getAverageSubSel();
            } else if (sp.getObjectVar().hasValue()) {
                size = size * c.getAverageObjSel();
            }
            if (size > maxSize) {
                endpoint = cd;
                maxSize = size;
            }
        }
        return endpoint;
    }

    public List<StatementSource> sourceWiseRankingAndSkipping(StatementPattern sp, List<StatementSource> capableDatasources) {
        
        // Daw Source Selection requires bounded predicates
        if (!sp.getPredicateVar().hasValue()) {
            //System.out.println("unbounded predicate");
            return capableDatasources;
        }
        String predicate = sp.getPredicateVar().getValue().stringValue();
        StatementSource selectedSource = getMaxSizeSource(capableDatasources, sp);
        //System.out.println("selectedSource: "+selectedSource);
        if (selectedSource == null) { // the index does not have information about the capable sources
            //System.out.println("index without information about capable source for predicate "+predicate);
            return capableDatasources;
        }
        EndpointSummary e = summaries.get(endpoints.get(selectedSource.getEndpointID()));
        List<StatementSource> rankedSources = new ArrayList<StatementSource>();
        Capability c = e.getCapability(predicate);
        double unionMIPsSetSize = c.getTotal();
        Vector<Long> unionMIPs = c.getMIPVector();
        rankedSources.add(selectedSource);
        capableDatasources.remove(selectedSource);
        while (!capableDatasources.isEmpty()) {
            selectedSource = null;
            double maxNewTriples = 0;
            
            for (StatementSource ss : capableDatasources) {
                e = summaries.get(endpoints.get(ss.getEndpointID()));
                //System.out.println("endpoint: "+ss.getEndpointID());
                //System.out.println("predicate: "+predicate);
                //System.out.println("e: "+e);
                c = e.getCapability(predicate);
                Vector<Long> MIPs = c.getMIPVector();
                double MIPsSetSize = c.getTotal();
                
                if (sp.getSubjectVar().hasValue()) {
                    MIPsSetSize = MIPsSetSize * c.getAverageSubSel();
                } else if (sp.getObjectVar().hasValue()) {
                    MIPsSetSize = MIPsSetSize * c.getAverageObjSel();
                }
                //System.out.println("unionMIPsSetSize: "+unionMIPsSetSize+". MIPsSetSize: "+MIPsSetSize);
                double overlapSize = getOverlap(unionMIPs,MIPs, unionMIPsSetSize, MIPsSetSize);
                // how many of the triples accessibles through ss are new ? 
                double newTriples = MIPsSetSize - overlapSize;  
                if (newTriples > maxNewTriples) {
                    selectedSource = ss;
                    maxNewTriples = newTriples;
                }
            }
            double curThresholdVal = maxNewTriples / unionMIPsSetSize;
            //System.out.println("maxNewTriples: "+maxNewTriples+". curThresholdVal: "+curThresholdVal);
            if (curThresholdVal > this.threshold) {
                rankedSources.add(selectedSource);
                e = summaries.get(endpoints.get(selectedSource.getEndpointID()));
                c = e.getCapability(predicate);
                Vector<Long> selectedMIPs = c.getMIPVector();
                double selectedMIPsSize = c.getTotal();
                unionMIPs = makeUnion(unionMIPs, selectedMIPs);
                double r = getResemblance(unionMIPs, selectedMIPs);
                unionMIPsSetSize = Math.ceil((unionMIPsSetSize + selectedMIPsSize) / (r + 1));
            } else { 
                break;
            } 
            capableDatasources.remove(selectedSource);
        }
        return rankedSources;
    }
    
    public static Vector<Long> makeUnion(Vector<Long> v1, Vector<Long> v2) {
        int maxSize = Math.max(v1.size(), v2.size());
        Vector<Long> v3 = new Vector<Long>();
        double resemblance = getResemblance(v1,v2);
        //System.out.println("v1: "+v1);
        //System.out.println("v2: "+v2);
        for (int i=0; i<maxSize; i++) {
            long x1 = (v1.size()-1<i)?Long.MAX_VALUE:v1.get(i);
            long x2 = (v2.size()-1<i)?Long.MAX_VALUE:v2.get(i);
            //System.out.println("i: "+i+". x1: "+x1+". x2: "+x2);
            if (x1==-1) v3.add(x2);
            else if (x2==-1) v3.add(x1);
            else v3.add(Math.min(x1, x2));
        }
        //System.out.println("v3: "+v3);
        return v3;
/*    
        Vector<Integer> union = new Vector<Integer>();
        
        int i = 0;
        for ( ; i < v1.size() && i < v2.size(); i++) {
            if (v1.get(i) <= v2.get(i)) {
                union.add(v1.get(i));
            } else {
                union.add(v2.get(i));
            }
        }
        for ( ; i < v1.size(); i++) {
            union.add(v1.get(i));
        }
        for ( ; i < v2.size(); i++) {
            union.add(v2.get(i));
        }
        
        return union;*/
    }
    
    public static double getOverlap(Vector<Long> v1, Vector<Long> v2, double sizeS1, double sizeS2) {
        //System.out.println("getOverlap");
        double resemblance = getResemblance(v1, v2);
        //System.out.println("resemblance: "+resemblance);
        //System.out.println("sizeS1: "+sizeS1+". sizeS2: "+sizeS2);
        //double size = Math.min(sizeS1, sizeS2);
        return (resemblance*(sizeS1 + sizeS2))/(resemblance+1);
    }
    
    public static double getResemblance(Vector<Long> v1, Vector<Long> v2) {
        //System.out.println("getResemblance");
        //System.out.println("v1: "+v1);
        //System.out.println("v2: "+v2);
        ////Vector<Integer> inter = new Vector<Integer>();
        ////inter.addAll(v1);
        ////inter.retainAll(v2);
        //System.out.println("inter: "+inter);
        ////return (((double) inter.size())/Math.min(v1.size(), v2.size()));
        int minSize = Math.min(v1.size(), v2.size());
        int count = 0;
        for (int i=0; i < minSize; i++) {
            if (v1.get(i) != -1 && (v1.get(i).equals(v2.get(i)))) {
                count++;
            } //else {
                //System.out.println("in position "+i+": "+v1.get(i)+" is different from "+v2.get(i));
            //}
        }
        return count/(double) minSize;
    }
}
