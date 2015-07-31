package com.fluidops.fedx.optimizer;

import java.util.*;
import java.io.*;

import org.openrdf.query.QueryLanguage;
//import org.openrdf.query.Query;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.query.parser.sparql.SPARQLParserFactory;
import org.openrdf.query.parser.QueryParser;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Var;
import org.openrdf.queryrender.sparql.SparqlTupleExprRenderer;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.impl.EmptyBindingSet;
import com.fluidops.fedx.structures.Endpoint;
import com.fluidops.fedx.Config;
import com.fluidops.fedx.evaluation.TripleSource;
import org.openrdf.query.algebra.helpers.StatementPatternCollector;
/*
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.algebra.*;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.syntax.*;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.sparql.expr.ExprList;

import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.Locator;*/

// javac -cp ".:/Users/montoya-g/Downloads/apache-jena-2.11.0/lib/*" fedra.java
// java -cp ".:/Users/montoya-g/Downloads/apache-jena-2.11.0/lib/*" fedra testing/three/query1.sparql testing/three/endpointDescription testing/three/publicEndpoints testing/three/containedIn testing/three/origin /Users/montoya-g/Dropbox/presentations/federatedExecution/testing/three/viewsDefinition/
class FedraQueryRewriter {

    private TupleExpr query;
    private List<StatementPattern> stmts;
    private HashMap<String,Endpoint> endpoints;
    private HashMap<StatementPattern, HashSet<TreeSet<Endpoint>>> selectedSources;
    private ArrayList<ArrayList<StatementPattern>> bgps;
    private TreeMap<String, TriplePatternFragment> fragments;
    private boolean random;
    private HashMap<Endpoint, Set<StatementPattern>> options;

    public FedraQueryRewriter(TupleExpr query, List<StatementPattern> stmts, List<Endpoint> endpoints) {
        this.query = query;
        this.stmts = stmts;
        this.endpoints = new HashMap<String,Endpoint>();
        for (Endpoint e : endpoints) {
            this.endpoints.put(e.getEndpoint(), e);
        }
        //System.out.println("endpoints: "+endpoints);
        this.selectedSources = new HashMap<StatementPattern, HashSet<TreeSet<Endpoint>>>();
        this.bgps = getBGPs(query);
        loadFragments(Config.getConfig().getProperty("FragmentsDefinitionFolder"), Config.getConfig().getProperty("FragmentsSources"));
        loadEndpoints(Config.getConfig().getProperty("EndpointsFile"));
        this.random = Boolean.parseBoolean(Config.getConfig().getProperty("Random"));
        this.options = new HashMap<Endpoint, Set<StatementPattern>>();
        //System.out.println("endpoints: "+endpoints);
        //System.out.println("fragments: "+fragments);
        //System.out.println("endpoints: "+endpoints);
    }

    private static ArrayList<ArrayList<StatementPattern>> getBGPs(TupleExpr query) {
        //System.out.println("query: "+query);
        ArrayList<ArrayList<StatementPattern>> bgps = null;
        try {
            BGPVisitor bgpv = new BGPVisitor();
            query.visit(bgpv);
            bgps =  bgpv.getBGPs();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return bgps;
    }

    public HashMap<StatementPattern, HashSet<TreeSet<Endpoint>>> getSelectedSources() {

        return this.selectedSources;
    }

    public HashMap<Endpoint, Set<StatementPattern>> getOptions() {

        return this.options;
    } 

    private HashSet<TreeSet<Endpoint>> getEndpoints(HashSet<List<TriplePatternFragment>> selectedFragments) {

        //System.out.println("selected fragments: "+selectedFragments);
        //System.out.println("endpoints: "+this.endpoints);
        HashSet<TreeSet<Endpoint>> fs = new HashSet<TreeSet<Endpoint>>();
        for (List<TriplePatternFragment> fragmentList : selectedFragments) {
                TreeSet<Endpoint> f = new TreeSet<Endpoint>(new EndpointComparator());
                for (TriplePatternFragment fragment : fragmentList) {
                    List<String> l = fragment.getAllSources();
                    for (String en : l) {
                        if (this.endpoints.containsKey(en)) {
                            f.add(this.endpoints.get(en));
                        }
                    }
                }
                fs.add(f);
        }
        //System.out.println("selected endpoints: "+fs);
	return fs;
    }

    public HashMap<StatementPattern, HashSet<TreeSet<Endpoint>>> sourceSelectionPerTriple() {

        HashMap<StatementPattern, HashSet<TreeSet<Endpoint>>> candidates = new HashMap<StatementPattern, HashSet<TreeSet<Endpoint>>>();
        for (StatementPattern sp : this.stmts) {
            HashSet<List<TriplePatternFragment>> selectedFragments = new HashSet<List<TriplePatternFragment>>();
            //System.out.println("considering sp: "+sp);
            for (String fn : this.fragments.keySet()) {
                TriplePatternFragment f = this.fragments.get(fn);
                if (f.canAnswer(sp)) {
                    HashSet<List<TriplePatternFragment>> redundantFragments = new HashSet<List<TriplePatternFragment>>();
                    boolean toAdd = true;
                    List<List<TriplePatternFragment>> includeWith = new ArrayList<List<TriplePatternFragment>>(); 
                    //System.out.println("trying to add: "+fn+" from dataset: "+f.getDataset());
                    //System.out.println("is f an exact match of sp? "+f.exactMatch(sp));
                    //System.out.println("so far there are "+selectedFragments.size()+ " fragments");
                    for (List<TriplePatternFragment> l :  selectedFragments) {
                        TriplePatternFragment f2 = l.get(0);
                        //System.out.println("checking already added fragment: "+f2.triple+" from dataset: "+f2.getDataset()+" in fragment list: "+l);
                        //System.out.println("is f2 an exact match of sp? "+f2.exactMatch(sp));
                        //System.out.println("f.getDataset().equals(f2.getDataset()): "+f.getDataset().equals(f2.getDataset()));
                        //System.out.println("f.containedBy(f2): "+f.containedBy(f2));
                        //System.out.println("f.contains(f2): "+f.contains(f2));
                        //System.out.println("f.contains(sp): "+f.contains(sp));
                        //System.out.println("f2.contains(sp): "+f2.contains(sp));
                        if (f.contains(sp) && f2.contains(sp) && (f.contains(f2) || f.containedBy(f2))) {
                            includeWith.add(l);
                            toAdd = false;
                        } else if (f.containedBy(f2)) {
                            toAdd = false;                                                                                                                                          
                            break;
                        } else if (f.contains(f2)) {
                            redundantFragments.add(l);
                        }
                    }
                    //System.out.println("removing "+redundantFragments.size()+" fragments");
                    for (List<TriplePatternFragment> l : redundantFragments) {
                        selectedFragments.remove(l);
                    }
                    for (List<TriplePatternFragment> fl : includeWith) {
                        if (!fl.contains(f)) {
                            selectedFragments.remove(fl);
                            fl.add(f);
                            selectedFragments.add(fl);
                        }
                    }
                    if (toAdd) {
                        ArrayList<TriplePatternFragment> l = new ArrayList<TriplePatternFragment>();
                        l.add(f);
                        selectedFragments.add(l);
                    }
                }
            }
            //System.out.println("selected fragments: "+selectedFragments);
            HashSet<TreeSet<Endpoint>> endpoints = getEndpoints(selectedFragments);
            candidates.put(sp, endpoints);
        }
        return candidates;
    }

    private static void obtainInstance(HashMap<StatementPattern, HashSet<TreeSet<Endpoint>>> candidateSources, 
                                       TreeSet<StatementPattern> elements, 
                                       TreeMap<Pair<Endpoint, Integer>, HashSet<StatementPattern>> collections) { 
        HashMap<Endpoint, HashSet<StatementPattern>> endpoints = new HashMap<Endpoint, HashSet<StatementPattern>>();
        // Obtaining the instance of the minimal set covering problem
        for (StatementPattern sp : candidateSources.keySet()) {
            HashSet<TreeSet<Endpoint>> fs = candidateSources.get(sp);
            elements.add(sp);
            for (TreeSet<Endpoint> f : fs) {
                for (Endpoint e : f) {
                    HashSet<StatementPattern> sps = endpoints.get(e);
                    if (sps == null) {
                        sps = new HashSet<StatementPattern>();
                    }
                    sps.add(sp);
                    endpoints.put(e, sps);
                }
            }
        }
        for (Endpoint e : endpoints.keySet()) {
            int i = 0;
            HashSet<StatementPattern> triples = endpoints.get(e);
            HashSet<StatementPattern> triplesAux = new HashSet<StatementPattern>(triples);
            while (triplesAux.size() > 0) {
                StatementPattern t = triplesAux.iterator().next();
                HashSet<StatementPattern> c = getConnected(t, triples);
                //System.out.println("connected to triple "+t+" are: "+c);
                collections.put(new Pair<Endpoint, Integer>(e, i), c);
                triplesAux.removeAll(c);
                i++;
            }
        }
    }

    protected static HashSet<StatementPattern> getConnected(StatementPattern t, HashSet<StatementPattern> triples) {

        HashSet<StatementPattern> triplesAux = new HashSet<StatementPattern>(triples);
        HashSet<StatementPattern> connected = new HashSet<StatementPattern>();
        connected.add(t);
        int size = 0;
        while (size != connected.size()) {
            size = connected.size();
            HashSet<StatementPattern> toRemove = new HashSet<StatementPattern>();
            for (StatementPattern tAux : triplesAux) {
                if (joinAny(tAux, connected)) {
                    connected.add(tAux);
                    toRemove.add(tAux);
                }
            }
            for (StatementPattern tAux : toRemove) {
                triplesAux.remove(tAux);
            }
        }
        return connected;
    }

    public static boolean joinAny(StatementPattern t, HashSet<StatementPattern> triples) {
        boolean join = false;
        for (StatementPattern tAux : triples) {
            if (join(t, tAux)) {
                join = true;
                break;
            }
        }
        return join;
    }

    public static boolean join(StatementPattern t1, StatementPattern t2) {

        HashSet<String> varsT1 = new HashSet<String>();
        HashSet<String> varsT2 = new HashSet<String>();
        if (t1.getSubjectVar().hasValue()) {
            varsT1.add(t1.getSubjectVar().getValue().stringValue());
        } else {
            // check if we need to add '?'
            varsT1.add(t1.getSubjectVar().getName());
        }
        if (!t1.getPredicateVar().hasValue()) {
            varsT1.add(t1.getPredicateVar().getName());
        }
        if (t1.getObjectVar().hasValue()) {
            varsT1.add(t1.getObjectVar().getValue().stringValue());
        } else {
            varsT1.add(t1.getObjectVar().getName());
        }
        if (t2.getSubjectVar().hasValue()) {
            varsT2.add(t2.getSubjectVar().getValue().stringValue());
        } else {
            varsT2.add(t2.getSubjectVar().getName());
        }
        if (t2.getPredicateVar().hasValue()) {
            varsT2.add(t2.getPredicateVar().getName());
        }
        if (t2.getObjectVar().hasValue()) {
            varsT2.add(t2.getObjectVar().getValue().stringValue());
        } else {
            varsT2.add(t2.getObjectVar().getName()); 
        }
        //System.out.println("t1: "+t1+". varsT1: "+varsT1);
        //System.out.println("t2: "+t2+". varsT2: "+varsT2);
        varsT1.retainAll(varsT2);
        //System.out.println("intersection: "+varsT1);
        return varsT1.size()>0;
    }

    private static TreeMap<Pair<Endpoint, Integer>, HashSet<StatementPattern>> getMinimalSetCovering(TreeSet<StatementPattern> elements, 
                                       TreeMap<Pair<Endpoint, Integer>, HashSet<StatementPattern>> collections, boolean random) {
        TreeSet<StatementPattern> elementsToCover = new TreeSet<StatementPattern> (new StatementPatternComparator());
        elementsToCover.addAll(elements);
        TreeMap<Pair<Endpoint, Integer>, HashSet<StatementPattern>> selected = new TreeMap<Pair<Endpoint, Integer>, HashSet<StatementPattern>>(new PairComparator());
        //System.out.println("elements: "+elements);
        while(elementsToCover.size() > 0) {
            TreeSet<Pair<Endpoint, Integer>> c = new TreeSet<Pair<Endpoint, Integer>>(new PairComparator());
            //System.out.println("collections: "+collections);
            for (Pair<Endpoint, Integer> k : collections.keySet()) {
                if (c.size() == 0) {
                    c.add(k);
                } else {
                    TreeSet<StatementPattern> tmp1 = new TreeSet<StatementPattern>(elementsToCover);
                    tmp1.retainAll(collections.get(c.first()));
                    TreeSet<StatementPattern> tmp2 = new TreeSet<StatementPattern>(elementsToCover);
                    tmp2.retainAll(collections.get(k));
                    if (tmp1.size() < tmp2.size()) {
                        c.clear();
                        c.add(k);
                    } else if (tmp1.size() == tmp2.size()) {
                        c.add(k);
                    }
                }
            }
            Iterator<Pair<Endpoint, Integer>> it = c.iterator();
            Pair<Endpoint, Integer> e = it.next();
            if (random) {
                int elm = (int) (Math.random()*c.size());
                for (int i = 0; i < elm && it.hasNext(); i++) {
                    e = it.next();
                }
            }
            elementsToCover.removeAll(collections.get(e));
            selected.put(e, collections.get(e));
            collections.remove(e);
        }
        return selected;
    }

    private static HashMap<StatementPattern, HashSet<TreeSet<Endpoint>>> select(HashMap<StatementPattern, HashSet<TreeSet<Endpoint>>> candidateSources, TreeMap<Pair<Endpoint, Integer>, HashSet<StatementPattern>> selected) {

        HashMap<StatementPattern, HashSet<TreeSet<Endpoint>>> ss = new HashMap<StatementPattern, HashSet<TreeSet<Endpoint>>>();
        HashMap<StatementPattern, TreeSet<Endpoint>> ssAux = new HashMap<StatementPattern, TreeSet<Endpoint>>();
        for (Pair<Endpoint, Integer> s : selected.keySet()) {
            Endpoint endpoint = s.getFirst();
            HashSet<StatementPattern> triples = selected.get(s);
            //System.out.println("s: "+s+". triples: "+triples);
            for (StatementPattern t : triples) {
                TreeSet<Endpoint> endpoints = ssAux.get(t);
                if (endpoints == null) {
                    endpoints = new TreeSet<Endpoint>(new EndpointComparator());
                }
                endpoints.add(endpoint);
                ssAux.put(t, endpoints);
            }
        }
        for (StatementPattern sp : candidateSources.keySet()) {
            HashSet<TreeSet<Endpoint>> fs = candidateSources.get(sp);
            TreeSet<Endpoint> tSelected = ssAux.get(sp);
            //System.out.println("selected sources for "+sp+" are: "+fs);
            HashSet<TreeSet<Endpoint>> es = new HashSet<TreeSet<Endpoint>>();
            for (TreeSet<Endpoint> f : fs) {
                TreeSet<Endpoint> newSelected = new TreeSet<Endpoint>(new EndpointComparator());
                newSelected.addAll(tSelected);
                newSelected.retainAll(f);
                es.add(newSelected);
            }
            ss.put(sp, es);
        }
        return ss;
    }

    private static HashMap<StatementPattern, HashSet<TreeSet<Endpoint>>> filter(HashMap<StatementPattern, HashSet<TreeSet<Endpoint>>> candidateSources, ArrayList<StatementPattern> bgp) {
        HashMap<StatementPattern, HashSet<TreeSet<Endpoint>>> result = new HashMap<StatementPattern, HashSet<TreeSet<Endpoint>>> ();
        for (StatementPattern sp : candidateSources.keySet()) {
            if (bgp.contains(sp)) {
                 result.put(sp, candidateSources.get(sp));
            }
        }
        return result;
    }

    private static void update(HashMap<StatementPattern, HashSet<TreeSet<Endpoint>>> candidateSources, HashMap<StatementPattern, HashSet<TreeSet<Endpoint>>> selected) {

        for (StatementPattern sp : candidateSources.keySet()) {                                                                                                  
            if (selected.containsKey(sp)) {
                candidateSources.put(sp, selected.get(sp));
            }
        }
    }

    public void performSourceSelection() { 
        
        HashMap<StatementPattern, HashSet<TreeSet<Endpoint>>> candidateSources = sourceSelectionPerTriple();
        //System.out.println("candidate sources: "+candidateSources);
        for (StatementPattern sp : candidateSources.keySet()) {
            HashSet<TreeSet<Endpoint>> sources = candidateSources.get(sp);
            if (sources.size() == 1) {
                Iterator<TreeSet<Endpoint>> it = sources.iterator();
                TreeSet<Endpoint> equivalentSources = it.next();
                for (Endpoint es : equivalentSources) {
                    Set<StatementPattern> optionalStatements = options.get(es);
                    if (optionalStatements == null) {
                        optionalStatements = new HashSet<StatementPattern>();
                    }
                    optionalStatements.add(sp);
                    options.put(es, optionalStatements);
                }
            }
        }
        // Priority is given to evaluate triple patterns that belong to the same basic graph pattern in as less endpoints as possible
        for (ArrayList<StatementPattern> bgp : bgps) {
            ArrayList<StatementPattern> bgp2 = new ArrayList<StatementPattern>();
            for (StatementPattern sp : bgp) {
                if (candidateSources.get(sp).size()>1) {
                    HashSet<TreeSet<Endpoint>> fragments = candidateSources.get(sp);
                    Iterator<TreeSet<Endpoint>> it = fragments.iterator();
                    TreeSet<Endpoint> intersection = new TreeSet<Endpoint>(it.next());
                    while (it.hasNext()) {
                        TreeSet<Endpoint> f = it.next();
                        intersection.retainAll(f);
                    }
                    if (intersection.size() > 0) {
                        fragments.clear();
                        fragments.add(intersection);
                        candidateSources.put(sp, fragments);
                    }
                }
            }
            for (StatementPattern sp : bgp) {                                                                                                                                       
                if (candidateSources.get(sp).size()==1) {
                    bgp2.add(sp);
                }
            }
            HashMap<StatementPattern, HashSet<TreeSet<Endpoint>>> bgpCandidateSources = filter(candidateSources, bgp2);
            TreeSet<StatementPattern> elements = new TreeSet<StatementPattern>(new StatementPatternComparator());
            TreeMap<Pair<Endpoint, Integer>, HashSet<StatementPattern>> collections = new TreeMap<Pair<Endpoint, Integer>, HashSet<StatementPattern>>(new PairComparator());
            obtainInstance(bgpCandidateSources, elements, collections);
            TreeMap<Pair<Endpoint, Integer>, HashSet<StatementPattern>> selectedSubsets = getMinimalSetCovering(elements, collections, this.random);
            bgpCandidateSources = select(bgpCandidateSources, selectedSubsets);
            update(candidateSources, bgpCandidateSources);
        }
        this.selectedSources = candidateSources;
        //System.out.println("candidate sourcesB: "+candidateSources);

        // Then a global choice is done for all the triple patterns, selecting as few endpoints as possible.
//        TreeSet<Pair<StatementPattern, Integer>> elements = new TreeSet<Pair<StatementPattern, Integer>>(new PairComparator());
//        TreeMap<Endpoint, TreeSet<Pair<StatementPattern, Integer>>> collections = new TreeMap<Endpoint, TreeSet<Pair<StatementPattern, Integer>>>(new EndpointComparator());
//        obtainInstance(candidateSources, elements, collections);                                                                                                                
//        TreeSet<Endpoint> selectedSubsets = getMinimalSetCovering(elements, collections, this.random);
//        candidateSources = select(candidateSources, selectedSubsets);
        //System.out.println("candidate sourcesC: "+candidateSources);

        // The remaining choices are done randomly to select one source for each different data fragment
        //System.out.println("selected sources: "+selectedSources);
    }

    public void loadEndpoints (String file) {

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String l = br.readLine();
            while (l!=null) {
                StringTokenizer st = new StringTokenizer(l);
                if (st.hasMoreTokens()) {
                    String fragment = st.nextToken();
                    //endpoints.add(endpoint);
                    TriplePatternFragment f = this.fragments.get(fragment);
                    while (st.hasMoreTokens()) {
                        String endpoint = st.nextToken();
                        f.addSource(endpoint);
                    }
                    // this.fragments.put(fragment, f);
                }
                l = br.readLine();
            }
            br.close();
            
        } catch (IOException e) {
            System.err.println("Problems reading file: "+file);
            System.exit(1);
        }
    }

    public static HashMap<String, String> loadSources (String file) {                                                                                                                                       

        HashMap map = new HashMap<String, String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String l = br.readLine();
            while (l!=null) {
                StringTokenizer st = new StringTokenizer(l);
                if (st.hasMoreTokens()) {
                    String fragment = st.nextToken();
                    String source =  st.nextToken();
                    map.put(fragment, source);
                }
                l = br.readLine();
            }
            br.close();

        } catch (IOException e) {
            System.err.println("Problems reading file: "+file);
            System.exit(1);
        }
        return map;
    }


    private static String getQuery(String file) {

        String query = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String l = br.readLine();
            while (l!=null) {
                query = query + l + "\n";
                l = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            System.err.println("Problems reading file: "+file);
            System.exit(1);
        }
        return query;
    }

    private StatementPattern getStatementPattern(TupleExpr te) {

        List<StatementPattern> stmts = StatementPatternCollector.process(te);
        StatementPattern sp = stmts.get(0);
        return sp;
    }

    public void loadFragments(String folder, String file) {

        HashMap<String, String> sources = loadSources(file);
        File f = new File(folder);
        File[] content = f.listFiles();
        this.fragments = new TreeMap<String, TriplePatternFragment>();
        //System.out.println("folder: "+folder);
        QueryParser qp = (new SPARQLParserFactory()).getParser();
        if (content != null) {
            for (File g : content) {
                try {
                    String path = g.getAbsolutePath();
                    //System.out.println("path: "+path);
                    String query = getQuery(path);
                    String baseURI = null;
                    ParsedQuery pq = qp.parseQuery(query, baseURI);
                    TupleExpr te = pq.getTupleExpr();
                    StatementPattern sp = getStatementPattern(te);
                    int i = path.lastIndexOf("/") + 1;
                    int j = path.lastIndexOf(".");
                    j = j < 0 ? path.length() : j;
                    String name = path.substring(i, j);
                    this.fragments.put(name, new TriplePatternFragment(sp, sources.get(name)));
                }  catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }
    }
} 
