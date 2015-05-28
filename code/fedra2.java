import java.util.*;
import java.io.*;

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
import com.hp.hpl.jena.util.Locator;

// javac -cp ".:/Users/montoya-g/Downloads/apache-jena-2.11.0/lib/*" fedra.java
// java -cp ".:/Users/montoya-g/Downloads/apache-jena-2.11.0/lib/*" fedra testing/three/query1.sparql testing/three/endpointDescription testing/three/publicEndpoints testing/three/containedIn testing/three/origin /Users/montoya-g/Dropbox/presentations/federatedExecution/testing/three/viewsDefinition/
class fedra2 {

    public static String getString(Node n) {
            String p = "";
            if (n.isURI()) {
                p = "<"+n.getURI().toString()+">";
            } else if (n.isLiteral()) {
                p = "\""+n.getLiteralLexicalForm()+"\"";
                String dt = n.getLiteralDatatypeURI();
                String lg = n.getLiteralLanguage();
                if (lg != null && !lg.equals("")) {
                    p = p + "@"+lg;
                }
                if (dt != null && !dt.equals("")) {
                    p = p + "^^<" + dt+">";
                }
            } else if (n.isVariable()) {
                p = "?"+n.getName();
            }
            return p;
    }

    public static void main (String args[]) throws Exception {

        String queryFile = args[0];
        String fragmentsDefinitionFolder = args[1];
        String endpointsFile = args[2];
        String fragmentsSources = args[3]; 
        boolean random = Boolean.parseBoolean(args[4]);
        String queryOutputFile = args[5];
        String availableSources = args[6];
        //String selectedEndpointsFile = args[6];
        Query query = QueryFactory.read(queryFile);
        ArrayList<ArrayList<Triple>> bgps = getBGPs(query);
        //System.out.println("bgps: "+bgps);
        ConjunctiveQuery q = new ConjunctiveQuery(queryFile);
        ArrayList<String> fs = new ArrayList<String>();
        HashMap<String, TriplePatternFragment> fragments = getViewDefinitions(fragmentsDefinitionFolder, fragmentsSources, fs);
        loadEndpoints(endpointsFile, fragments, availableSources);
        
        HashMap<Triple, Set<String>> options = new HashMap<Triple, Set<String>>();
        //long t1 = System.currentTimeMillis();
        //System.out.println("fragments: "+fragments);
        HashMap<Triple, HashSet<TreeSet<String>>> selectedSources = sourceSelection(random, fragments, q, bgps, options, fs);
        //System.out.println("options: "+options);
        //System.out.println("selected Sources: "+selectedSources);
        //long t2 = System.currentTimeMillis() - t1;
        //int nss = getNumberSelectedSourcesTripleWise(selectedSources);
        //System.out.print(q.getHead().getName()+"\t"+(t2/1000.0));
        //String queryOut = args[6];
        produceQueryWithServiceClauses(queryFile, selectedSources, queryOutputFile, options);
        //System.out.println(" Baseline source selection time: "+t4+" milliseconds, "+nss2+" selected sources." );
        //System.out.println("selected sources:\n"+selectedSources);
        //saveEndpoints(selectedSources, selectedEndpointsFile);
    }

    public static void loadEndpoints(String file, HashMap<String, TriplePatternFragment> fragments, String availableSources) {

        try {
            HashSet<String> ass = new HashSet<String>();
            BufferedReader br = new BufferedReader(new FileReader(availableSources));
            String l = br.readLine();
            while (l!=null) {
                ass.add(l);
                l = br.readLine();
            }
            br.close();
            br = new BufferedReader(new FileReader(file));
            l = br.readLine();
            while (l!=null) {
                StringTokenizer st = new StringTokenizer(l);
                if (st.hasMoreTokens()) {
                    String fragment = st.nextToken();
                    //endpoints.add(endpoint);
                    TriplePatternFragment f = fragments.get(fragment);
                    while (st.hasMoreTokens()) {
                        String endpoint = st.nextToken();
                        if (ass.contains(endpoint)) {
                            f.addSource(endpoint);
                        }
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

    private static ArrayList<ArrayList<Triple>> getBGPs(Query query) {

        ArrayList<ArrayList<Triple>> bgps = null;
        try {
            Op op = (new AlgebraGenerator()).compile(query);
            BGPVisitor bgpv = new BGPVisitor();
            OpWalker.walk(op, bgpv);
            bgps = bgpv.getBGPs();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return bgps;
    }

    public static int getNumberOfPublicEndpoints(HashMap<Triple, HashSet<String>> selectedSources, HashSet<String> publicEndpoints) {

        int pe = 0;
        for (Triple t : selectedSources.keySet()) {
            HashSet<String> hs = selectedSources.get(t);
            HashSet<String> aux = new HashSet<String>();
            aux.addAll(publicEndpoints);
            aux.retainAll(hs);
            pe = pe + aux.size();
        }
        return pe;
    }

    public static void saveEndpoints(HashMap<Triple, Set<String>> selectedSources, String usedEndpoints) throws Exception {

        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(usedEndpoints), "UTF-8"));
        //Collection<HashSet<String>> vs = selectedSources.values();
        HashSet<String> values = new HashSet<String>();
        for (Triple t : selectedSources.keySet()) {
            values.addAll(selectedSources.get(t));
        }
        for (String v : values) {
            out.write(v+"\n");
        }
        out.flush();
        out.close();
    }

    public static void produceQueryWithServiceClauses(String queryIn, HashMap<Triple, HashSet<TreeSet<String>>> selectedSources, String queryOut, HashMap<Triple, Set<String>> options) throws Exception {

        Query q = QueryFactory.read(queryIn);
        Op op = (new AlgebraGenerator()).compile(q);
        TransformerDeleteFilters t0 = new TransformerDeleteFilters();
        Op opBase = Transformer.transform(t0, op);
        //VisitorGetFilters vgf = new VisitorGetFilters();
        //OpWalker ow0 = new OpWalker();
        //ow0.walk(op, vgf);
        ExprList el = t0.getFilters();
        //System.out.println("opBase: "+opBase);
        Transform t = new TransformInOneDotOneSPARQL2(selectedSources, el, options);
        Op newOp = Transformer.transform(t, opBase);
        //System.out.println("new op: "+newOp);

        //VisitorCountTriples vct = new VisitorCountTriples();
        //OpWalker ow = new OpWalker();
        //ow.walk(newOp, vct);
        //int c = vct.getCount();
        //System.out.print(c+"\t");
        if (newOp instanceof OpNull) {
            //System.out.println("The available sources cannot answer the given query");
            selectedSources.clear();
            return;
        }// else {
        //    System.out.println("op type: "+ newOp.getClass());
        //}
        //System.out.println("op to transform in query: "+newOp);
        Query newQuery = OpAsQuery.asQuery(newOp);
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(queryOut), "UTF-8"));
        String newQ = newQuery.toString().replace("WHERE", " WHERE");
        out.write(newQ);
        out.flush();
        out.close();
        ConjunctiveQuery cq = new ConjunctiveQuery(newQuery, "newQ");
        List<Triple> body = cq.getBody();
        HashSet<Triple> set = new HashSet<Triple>();
        set.addAll(selectedSources.keySet());
        set.removeAll(body);
        for (Triple r : set) {
            selectedSources.remove(r);
        }
    }

    public static int getNumberSelectedSourcesTripleWise(HashMap<Triple, Set<String>> selectedSources) {

        int n = 0;
        for (Triple t : selectedSources.keySet()) {
            n = n + selectedSources.get(t).size();
        }
        return n;
    }    

    public static boolean equalOrigin(String e, String e2, String v, String v2, HashMap<String, HashMap<String, HashSet<String>>> origin) {
        
        return (origin.get(e) != null && origin.get(e).get(v) != null && origin.get(e2) != null && origin.get(e2).get(v2) != null)
                && origin.get(e).get(v).equals(origin.get(e2).get(v2));
    }

    public static int getNumberSelectedSourcesBaseline(HashSet<String> endpoints, HashMap<String, HashSet<String>> views, HashMap<String, Query> viewDefinition, ConjunctiveQuery q, HashMap<String, HashMap<String, Boolean>> cache) {
        int n = 0;
        for (Triple t : q.getBody()) {

            HashSet<String> selectedEndpoints = new HashSet<String>();

            for (String e : endpoints) {

                //HashSet<String> viewsInE = new HashSet<String>();
                //for (String v : views.get(e)) {
                    //System.out.println("selected endpoints for "+t+": "+selectedEndpoints);
                    if (makeAskTP(e, t,  cache)) {
                        selectedEndpoints.add(e);
                    }
                //}
            }
            n += selectedEndpoints.size();
        }
        return n;
    }

    public static HashMap<Triple, HashSet<TreeSet<String>>> sourceSelectionPerTriple(HashMap<String, TriplePatternFragment> fragments, ConjunctiveQuery q, ArrayList<String> fs) {
        
        HashMap<Triple, HashSet<TreeSet<String>>> candidates = new HashMap<Triple, HashSet<TreeSet<String>>>();
         
        for (Triple t : q.getBody()) {
            
            HashSet<List<TriplePatternFragment>> selectedFragments = new HashSet<List<TriplePatternFragment>>();
            //System.out.println("considering "+t);
            for (String fn : fs) {
                TriplePatternFragment f = fragments.get(fn);
                if (f.canAnswer(t)) {
                    HashSet<List<TriplePatternFragment>> redundantFragments = new HashSet<List<TriplePatternFragment>>();
                    boolean toAdd = true;
                    List<List<TriplePatternFragment>> includeWith = new ArrayList<List<TriplePatternFragment>>();
                    //System.out.println("trying to add: "+fn+" from dataset: "+f.getDataset());
                    //System.out.println("is f an exact match of sp? "+f.exactMatch(sp));
                    //System.out.println("so far selected fragments: "+selectedFragments);
                    for (List<TriplePatternFragment> l :  selectedFragments) {
                        TriplePatternFragment f2 = l.get(0);
                        //System.out.println("checking already added fragment: "+f2.triple+" from dataset: "+f2.getDataset()+" in fragment list: "+l);
                        //System.out.println("is f2 an exact match of sp? "+f2.exactMatch(sp));
                        //System.out.println("f.getDataset().equals(f2.getDataset()): "+f.getDataset().equals(f2.getDataset()));
                        //System.out.println("f.containedBy(f2): "+f.containedBy(f2));
                        //System.out.println("f.contains(f2): "+f.contains(f2));
                        //System.out.println("f.contains(t): "+f.contains(t));
                        //System.out.println("f2.contains(t): "+f2.contains(t));
                        if (f.contains(t) && f2.contains(t) && (f.contains(f2) || f.containedBy(f2))) {
                            includeWith.add(l);
                            toAdd = false;
                        } else if (f.containedBy(f2)) {
                            toAdd = false;
                            break;
                        } else if (f.contains(f2)) {
                            redundantFragments.add(l);
                        }
                    }
                    //System.out.println("removing fragments: "+redundantFragments);
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
            HashSet<TreeSet<String>> endpoints = getEndpoints(selectedFragments);
            candidates.put(t, endpoints);
        }
        return candidates;
    }

    private static HashSet<TreeSet<String>> getEndpoints(HashSet<List<TriplePatternFragment>> selectedFragments) {

        //System.out.println("selected fragments: "+selectedFragments);
        //System.out.println("endpoints: "+this.endpoints);
        HashSet<TreeSet<String>> fs = new HashSet<TreeSet<String>>();
        for (List<TriplePatternFragment> fragmentList : selectedFragments) {
                TreeSet<String> f = new TreeSet<String>();
                for (TriplePatternFragment fragment : fragmentList) {
                    List<String> l = fragment.getAllSources();
                    f.addAll(l);
                }
                fs.add(f);
        }
        //System.out.println("selected endpoints: "+fs);
        return fs;
    }
    
    public static HashMap<Triple, HashSet<TreeSet<String>>> sourceSelection(boolean random, HashMap<String, TriplePatternFragment> fragments, ConjunctiveQuery q, ArrayList<ArrayList<Triple>> bgps, HashMap<Triple, Set<String>> options, ArrayList<String> fns) {
        //HashMap<Triple, Set<String>> selectedSources;
        HashMap<Triple, HashSet<TreeSet<String>>> candidateSources = sourceSelectionPerTriple(fragments, q, fns);
        for (Triple t : candidateSources.keySet()) {
            HashSet<TreeSet<String>> sources = candidateSources.get(t);
            if (sources.size() == 1) {
                Iterator<TreeSet<String>> it = sources.iterator();
                TreeSet<String> ss = new TreeSet<String>(it.next());
                options.put(t,ss);
            }
        }
        //System.out.println("candidate sources: "+candidateSources);

        for (ArrayList<Triple> bgp : bgps) {
            ArrayList<Triple> bgp2 = new ArrayList<Triple>();
            for (Triple t : bgp) {
                if (candidateSources.get(t).size()>1) {
                    HashSet<TreeSet<String>> fs = candidateSources.get(t);
                    Iterator<TreeSet<String>> it = fs.iterator();
                    TreeSet<String> intersection = new TreeSet<String>(it.next());
                    while (it.hasNext()) {
                        TreeSet<String> f = it.next();
                        intersection.retainAll(f);
                    }
                    //System.out.println("intersection: "+intersection);
                    if (intersection.size() > 0) {
                        //System.out.println("changing");
                        fs.clear();
                        fs.add(intersection);
                        candidateSources.put(t, fs);
                    }
                }
            }
            for (Triple t : bgp) {
                if (candidateSources.get(t).size()==1) {
                    bgp2.add(t);
                }
            }
            //System.out.println("bgp2: "+bgp2);
            HashMap<Triple, HashSet<TreeSet<String>>> bgpCandidateSources = filter(candidateSources, bgp2);
            //System.out.println("bgpCandidateSources: "+bgpCandidateSources);
            TreeSet<Triple> elements = new TreeSet<Triple>(new TriplePatternComparator());
            TreeMap<Pair<String, Integer>, HashSet<Triple>> collections = new TreeMap<Pair<String, Integer>, HashSet<Triple>>(new PairComparator());
            obtainInstance(bgpCandidateSources, elements, collections);
            //System.out.println("elements: "+elements);
            //System.out.println("collections: "+collections);
            TreeMap<Pair<String, Integer>, HashSet<Triple>> selectedSubsets = getMinimalSetCovering(elements, collections, random);
            bgpCandidateSources = select(bgpCandidateSources, selectedSubsets);
            //System.out.println("bgpCandidateSources: "+bgpCandidateSources);
            update(candidateSources, bgpCandidateSources);
        }
        return candidateSources;
    }

    private static HashMap<Triple, HashSet<TreeSet<String>>> select(HashMap<Triple, HashSet<TreeSet<String>>> candidateSources, TreeMap<Pair<String, Integer>, HashSet<Triple>> selected) {
        //System.out.println("candidateSources: "+candidateSources);
        //System.out.println("selected: "+selected);
        //System.out.println("collections: "+collections);
        HashMap<Triple, HashSet<TreeSet<String>>> ss = new HashMap<Triple, HashSet<TreeSet<String>>>();
        HashMap<Triple, TreeSet<String>> ssAux = new HashMap<Triple, TreeSet<String>>();
        for (Pair<String, Integer> s : selected.keySet()) {
            String endpoint = s.getFirst();
            HashSet<Triple> triples = selected.get(s);
            //System.out.println("s: "+s+". triples: "+triples);
            for (Triple t : triples) {
                TreeSet<String> endpoints = ssAux.get(t);
                if (endpoints == null) {
                    endpoints = new TreeSet<String>();
                }
                endpoints.add(endpoint);
                ssAux.put(t, endpoints);
            }
        }
        for (Triple t : candidateSources.keySet()) {
            HashSet<TreeSet<String>> fragments = candidateSources.get(t);
            TreeSet<String> tSelected = ssAux.get(t);
            HashSet<TreeSet<String>> newFragments = new HashSet<TreeSet<String>>();
            for (TreeSet<String> sources : fragments) {
                TreeSet<String> newSelected = new TreeSet<String>(tSelected);
                newSelected.retainAll(sources);
                newFragments.add(newSelected);
            }
            ss.put(t, newFragments);
        }
        return ss;
    }

    private static HashMap<Triple, HashSet<TreeSet<String>>> filter(HashMap<Triple, HashSet<TreeSet<String>>> candidateSources, ArrayList<Triple> bgp) {
        HashMap<Triple, HashSet<TreeSet<String>>> result = new HashMap<Triple, HashSet<TreeSet<String>>> ();
        for (Triple t : candidateSources.keySet()) {
            if (bgp.contains(t)) {
                 result.put(t, candidateSources.get(t));
            }
        }
        return result;
    }

    private static void update(HashMap<Triple, HashSet<TreeSet<String>>> candidateSources, HashMap<Triple, HashSet<TreeSet<String>>> selected) {

        //System.out.println("candidateSources: "+candidateSources);
        //System.out.println("selected: "+selected);
        for (Triple t : candidateSources.keySet()) {
            if (selected.containsKey(t)) {
                //System.out.println("selected has key: "+t);
                candidateSources.put(t, selected.get(t));
            }
        }
    }
    private static void obtainInstance(HashMap<Triple, HashSet<TreeSet<String>>> candidateSources,
                                       TreeSet<Triple> elements,
                                       TreeMap<Pair<String, Integer>, HashSet<Triple>> collections) {
        // Obtaining the instance of the minimal set covering problem
        HashMap<String, HashSet<Triple>> endpoints = new HashMap<String, HashSet<Triple>>();

        for (Triple t : candidateSources.keySet()) {
            HashSet<TreeSet<String>> fs = candidateSources.get(t);
            //System.out.println("t type: "+t.getClass());
            //System.out.println("elements type: "+elements.getClass());
            elements.add(t);
            for (TreeSet<String> f : fs) {
                for (String e : f) {
                    HashSet<Triple> triples = endpoints.get(e);
                    if (triples == null) {
                        triples = new HashSet<Triple>();
                    }
                    triples.add(t);
                    endpoints.put(e, triples);
                }
            }
        }
        for (String e : endpoints.keySet()) {
            int i = 0;
            HashSet<Triple> triples = endpoints.get(e);
            HashSet<Triple> triplesAux = new HashSet<Triple>(triples);
            while (triplesAux.size() > 0) {
                Triple t = triplesAux.iterator().next();
                HashSet<Triple> c = getConnected(t, triples);
                //System.out.println("connected to triple "+t+" are: "+c);
                collections.put(new Pair<String, Integer>(e, i), c);
                triplesAux.removeAll(c);
                i++;
            }
        }
    }

    protected static HashSet<Triple> getConnected(Triple t, HashSet<Triple> triples) {

        HashSet<Triple> triplesAux = new HashSet<Triple>(triples);
        HashSet<Triple> connected = new HashSet<Triple>();
        connected.add(t);
        int size = 0;
        while (size != connected.size()) {
            size = connected.size();
            HashSet<Triple> toRemove = new HashSet<Triple>();
            for (Triple tAux : triplesAux) {
                if (joinAny(tAux, connected)) {
                    connected.add(tAux);
                    toRemove.add(tAux);
                }
            }
            for (Triple tAux : toRemove) {
                triplesAux.remove(tAux);
            }
        }
        return connected;
    }

    public static boolean joinAny(Triple t, HashSet<Triple> triples) {
        boolean join = false;
        for (Triple tAux : triples) {
            if (join(t, tAux)) {
                join = true;
                break;
            }
        }
        return join;
    }

    public static boolean join(Triple t1, Triple t2) {

        HashSet<String> varsT1 = new HashSet<String>();
        HashSet<String> varsT2 = new HashSet<String>();
        //if (t1.getSubject().isVariable()) {
            varsT1.add(getString(t1.getSubject()));
        //}
        if (t1.getPredicate().isVariable()) {
            varsT1.add(t1.getPredicate().getName());
        }
        //if (t1.getObject().isVariable()) {
            varsT1.add(getString(t1.getObject()));
        //}
        //if (t2.getSubject().isVariable()) {
            varsT2.add(getString(t2.getSubject()));
        //}
        if (t2.getPredicate().isVariable()) {
            varsT2.add(t2.getPredicate().getName());
        }
        //if (t2.getObject().isVariable()) {
            varsT2.add(getString(t2.getObject()));
        //}
        //System.out.println("t1: "+t1+". varsT1: "+varsT1);
        //System.out.println("t2: "+t2+". varsT2: "+varsT2);
        varsT1.retainAll(varsT2);
        //System.out.println("intersection: "+varsT1);
        return varsT1.size()>0;
    }

    private static TreeMap<Pair<String, Integer>, HashSet<Triple>> getMinimalSetCovering(TreeSet<Triple> elements,
                                       TreeMap<Pair<String, Integer>, HashSet<Triple>> collections, boolean random) {
        TreeMap<Pair<String, Integer>, HashSet<Triple>> selected = new TreeMap<Pair<String, Integer>, HashSet<Triple>>(new PairComparator());
        //System.out.println("elements: "+elements);
        TreeSet<Triple> elementsToCover = new TreeSet<Triple> (new TriplePatternComparator());
        elementsToCover.addAll(elements);
        while(elementsToCover.size() > 0) {
            TreeSet<Pair<String, Integer>> c = new TreeSet<Pair<String, Integer>>(new PairComparator());
            //System.out.println("collections: "+collections);
            //System.out.println("coveredElements: "+coveredElements);
            for (Pair<String, Integer> k : collections.keySet()) {
                //if (c.size() > 0) {
                //System.out.println("c: "+c+".collections.get(c.first()).size(): "+ collections.get(c.first()).size());}
                //System.out.println("k: "+k+".collections.get(k).size(): "+collections.get(k).size()); 
                if (c.size() == 0) {
                    c.add(k);
                } else {
                  TreeSet<Triple> tmp1 = new TreeSet<Triple>(elementsToCover);
                  tmp1.retainAll(collections.get(c.first()));
                  TreeSet<Triple> tmp2 = new TreeSet<Triple>(elementsToCover);
                  tmp2.retainAll(collections.get(k));
                  if (tmp1.size() < tmp2.size()) {
                    c.clear();
                    c.add(k);
                  } else if (tmp1.size() == tmp2.size()) {
                    c.add(k);
                  }
                }
            }
            Iterator<Pair<String, Integer>> it = c.iterator();
            Pair<String, Integer> e = it.next();
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

    public static void loadDate (String file, HashMap<String, HashMap<String, HashMap<String, String>>> date) {

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String l = br.readLine();
            while (l!=null) {
                StringTokenizer st = new StringTokenizer(l);
                if (st.hasMoreTokens()) {
                    String endpoint = st.nextToken();
                    HashMap<String, HashMap<String, String>> ds = date.get(endpoint);
                    if (ds == null) {
                        ds = new HashMap<String, HashMap<String, String>>();
                    }

                    if (st.hasMoreTokens()) {
                        String view = st.nextToken();
                        HashMap<String, String> es = ds.get(view);
                        if (es == null) {
                            es = new HashMap<String, String>();
                        }
                        while (st.hasMoreTokens()) {
                            String endpointOrigin = st.nextToken();
                            String d = st.nextToken();
                            es.put(endpointOrigin, d);
                        }
                        ds.put(view, es);
                    }

                    date.put(endpoint, ds);
                }
                l = br.readLine();
            }
            br.close();

        } catch (IOException e) {
            System.err.println("Problems reading file: "+file);
            System.exit(1);
        }
    }

    public static void loadDivergence (String file, HashMap<String, HashMap<String, HashMap<String, Long>>> divergence) {

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String l = br.readLine();
            while (l!=null) {
                StringTokenizer st = new StringTokenizer(l);
                if (st.hasMoreTokens()) {
                    String view = st.nextToken();
                    HashMap<String, HashMap<String, Long>> ds = divergence.get(view);
                    if (ds == null) {
                        ds = new HashMap<String, HashMap<String, Long>>();
                    }

                    if (st.hasMoreTokens()) {
                        String endpoint = st.nextToken();
                        HashMap<String, Long> es = ds.get(endpoint);
                        if (es == null) {
                            es = new HashMap<String, Long>();
                        }
                        while (st.hasMoreTokens()) {
                            String date = st.nextToken();
                            Long div = Long.parseLong(st.nextToken());
                            es.put(date, div);
                        }
                        ds.put(endpoint, es);
                    }

                    divergence.put(view, ds);
                }
                l = br.readLine();
            }
            br.close();

        } catch (IOException e) {
            System.err.println("Problems reading file: "+file);
            System.exit(1);
        }
    }

    public static void loadOrigin (String file, HashMap<String, HashMap<String, HashSet<String>>> origin) {
        
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String l = br.readLine();
            while (l!=null) {
                StringTokenizer st = new StringTokenizer(l);
                if (st.hasMoreTokens()) {
                    String endpoint = st.nextToken();
                    HashMap<String, HashSet<String>> os = origin.get(endpoint);
                    if (os == null) {
                        os = new HashMap<String, HashSet<String>>();
                    }
                    
                    if (st.hasMoreTokens()) {
                        String view = st.nextToken();
                        HashSet<String> es = os.get(view);
                        if (es == null) {
                            es = new HashSet<String>();
                        }
                        while (st.hasMoreTokens()) {
                            String endpointOrigin = st.nextToken();
                            es.add(endpointOrigin);
                        }
                        os.put(view, es);
                    }
                    
                    origin.put(endpoint, os);
                }
                l = br.readLine();
            }
            br.close();
            
        } catch (IOException e) {
            System.err.println("Problems reading file: "+file);
            System.exit(1);
        }
    }
    
    public static void loadContainment (String file, HashMap<String, HashSet<String>> containedIn) {
        
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String l = br.readLine();
            while (l!=null) {
                StringTokenizer st = new StringTokenizer(l);
                if (st.hasMoreTokens()) {
                    String view1 = st.nextToken();
                    if (st.hasMoreTokens()) {
                        HashSet<String> cs = containedIn.get(view1);
                        if (cs == null) {
                            cs = new HashSet<String>();
                        }
                        String view2 = st.nextToken();
                        cs.add(view2);
                        containedIn.put(view1, cs);
                    }
                }
                l = br.readLine();
            }
            br.close();
            
        } catch (IOException e) {
            System.err.println("Problems reading file: "+file);
            System.exit(1);
        }
    }
    
    public static void loadEndpoints (String file, HashSet<String> endpoints) {
        
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String l = br.readLine();
            while (l!=null) {
                StringTokenizer st = new StringTokenizer(l);
                if (st.hasMoreTokens()) {
                    String endpoint = st.nextToken();
                    endpoints.add(endpoint);
                }
                l = br.readLine();
            }
            br.close();
            
        } catch (IOException e) {
            System.err.println("Problems reading file: "+file);
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    public static void loadEndpointsDescription (String file, HashSet<String> endpoints, HashMap<String, HashSet<String>> views) {

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String l = br.readLine();
            while (l!=null) {
                StringTokenizer st = new StringTokenizer(l);
                if (st.hasMoreTokens()) {
                    String endpoint = st.nextToken();
                    endpoints.add(endpoint);
                    HashSet<String> vs = new HashSet<String>();
                    while (st.hasMoreTokens()) {
                        String view = st.nextToken();
                        vs.add(view);
                    }
                    views.put(endpoint, vs);
                }
                l = br.readLine();
            }
            br.close();
            
        } catch (IOException e) {
            System.err.println("Problems reading file: "+file);
            System.exit(1);
        }
    }

    public static HashMap<String, String> loadSources (String file, ArrayList<String> fs) {                                                                                                                

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
                    fs.add(fragment);
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

    public static HashMap<String, TriplePatternFragment> getViewDefinitions(String folder, String file, ArrayList<String> fs) {
        HashMap<String, String> sources = loadSources(file, fs);
        File f = new File(folder);
        File[] content = f.listFiles();
        HashMap<String, TriplePatternFragment> hm = new HashMap<String, TriplePatternFragment>();
        //System.out.println("folder: "+folder);
        if (content != null) {
            for (File g : content) {
                String path = g.getAbsolutePath();
                //System.out.println("path: "+path);
                ConjunctiveQuery cq = new ConjunctiveQuery(path);
                Triple t = cq.getBody().get(0);
                int i = path.lastIndexOf("/") + 1;
                int j = path.lastIndexOf(".");
                j = j < 0 ? path.length() : j;
                String name = path.substring(i, j);
                hm.put(name, new TriplePatternFragment(t, sources.get(name)));
            }
        }
        return hm;
    }

    public static boolean compatible(Node nv, Node nq) {

        return (nv.isVariable() || nq.isVariable() || nv.equals(nq));
    }

    public static boolean makeAsk(String endpointAddress, Triple t, Triple vs, Query view) {

        //System.out.println("view before: "+view+"\n triple: "+t);
        Op op = Algebra.compile(view);

        Node predV = vs.getPredicate();
        Node predQ = t.getPredicate();
        Node subV = vs.getSubject();
        Node subQ = t.getSubject();
        Node objV = vs.getObject();
        Node objQ = t.getObject();
        HashMap<Node,Node> change = new HashMap<Node,Node>();
        if (!predQ.isVariable() && predV.isVariable()) {
            change.put(predV, predQ);
        }
        if (!subQ.isVariable() && subV.isVariable()) {
            change.put(subV, subQ);
        }
        if (!objQ.isVariable() && objV.isVariable()) {
            change.put(objV, objQ);
        }
        Transform transf = new myTransformTriple(change);
        Op newOp = Transformer.transform(transf, op);
        //System.out.println("new op: "+newOp);
        Query newQuery = OpAsQuery.asQuery(newOp);

        Query q = QueryFactory.create();
        q.setQueryAskType();
        q.setQueryPattern(newQuery.getQueryPattern());

        //BasicPattern bp = new BasicPattern();
        //bp.add(t);
        //Element elt = new ElementTriplesBlock(bp);
        //q.setQueryPattern(elt);
        //String s = "ASK { "+ t.getSubject() +" " +t.getPredicate() + " " +t.het "}";
        //System.out.println("query: "+q+"\n endpoint: "+endpointAddress);
        QueryEngineHTTP queryExec = new QueryEngineHTTP(endpointAddress, q);
        //QueryExecution queryExec = QueryExecutionFactory.sparqlService(endpointAddress, q);
        boolean b = queryExec.execAsk();
        //System.out.println("view after: "+view);
        //System.out.println("result: "+b);
        queryExec.close();
        return b;
    }

    public static boolean makeAskTP(String endpointAddress, Triple t, HashMap<String, HashMap<String, Boolean>> cache) {
        HashMap<String, Boolean> perEndpoint = cache.get(endpointAddress);
        if (perEndpoint ==  null) {
            perEndpoint = new HashMap<String, Boolean>();
        }
        if (perEndpoint.containsKey(t.toString())) {
            return perEndpoint.get(t.toString());
        }
        Query q = QueryFactory.create();
        q.setQueryAskType();
        BasicPattern bp = new BasicPattern();
        bp.add(t);
        Element elt = new ElementTriplesBlock(bp);
        q.setQueryPattern(elt);
        String query = "ASK { "+getString(t.getSubject())+" "+getString(t.getPredicate())+" "+getString(t.getObject())+" } ";
        //System.out.println("sending query: "+query+" to endpoint "+endpointAddress);
        QueryEngineHTTP queryExec = new QueryEngineHTTP(endpointAddress, query);
        boolean b = queryExec.execAsk();
        //System.out.println(b+" returned");
        queryExec.close();
        perEndpoint.put(t.toString(), b);
        cache.put(endpointAddress, perEndpoint);
        return b;
    }

/*
    public static boolean makeAskTP(String endpointAddress, Triple t, HashMap<String, HashMap<String, Boolean>> cache) {

        QueryExecution queryExec = null;
        boolean b = false;
        HashMap<String, Boolean> perEndpoint = cache.get(endpointAddress);
        if (perEndpoint ==  null) {
            perEndpoint = new HashMap<String, Boolean>();
        }
        if (perEndpoint.containsKey(t.toString())) {
            return perEndpoint.get(t.toString());
        }
      try {
        //FileManager fm = FileManager.get();
        //Iterator<Locator> it = fm.locators();
        //int i = 0;
        //while (it.hasNext()) {
        //    Locator l = it.next();
        //    System.out.println("locator: "+l.getName());
        //    i++;
        //}
        //System.out.println("currently there are "+i+" locators");
        Query q = QueryFactory.create();
        q.setQueryAskType();
        BasicPattern bp = new BasicPattern();
        bp.add(t);
        Element elt = new ElementTriplesBlock(bp);
        q.setQueryPattern(elt);
        //QueryEngineHTTP queryExec = new QueryEngineHTTP(endpointAddress, q);
        queryExec = QueryExecutionFactory.sparqlService(endpointAddress, q);
        b = queryExec.execAsk();
        //queryExec.close();
        //return b;
      } catch (org.apache.http.conn.ssl.SSLInitializationException e) {
        System.out.println("An exception has occurred when doing the ASKs: "+e.getMessage());
        //e.printStackTrace();
        //System.exit(1);
      } finally {
        //System.out.println(t+" in "+endpointAddress);
        queryExec.close();
        perEndpoint.put(t.toString(), b);
        cache.put(endpointAddress, perEndpoint);
        //System.out.println("closed");
      }
      return b;
    }
*/
    public static boolean canAnswer(String e, Query view, Triple t, HashMap<String, HashMap<String, Boolean>> cache) {

        Op op = Algebra.compile(view);
        myVisitor123 mv = new myVisitor123();
        OpWalker ow = new OpWalker();
        ow.walk(op, mv);
        List<Triple> viewSubgoals = mv.getTriples();

        for (Triple vs : viewSubgoals) {
            Node predV = vs.getPredicate();
            Node predQ = t.getPredicate();
            Node subV = vs.getSubject();
            Node subQ = t.getSubject();
            Node objV = vs.getObject();
            Node objQ = t.getObject(); // && makeAsk(e, t, vs, view)
            //System.out.println("[canAnswer] "+t+" in view "+view+" using "+vs+" in endpoint "+e); // && makeAskTP(e, t, cache)
            //System.out.println("preds compatible: "+compatible(predV, predQ));
            //System.out.println("subj compatible: "+compatible(subV, subQ));
            //System.out.println("obj compatible: "+compatible(objV, objQ));
            if (compatible(predV, predQ) && compatible(subV, subQ) && compatible(objV, objQ) && makeAskTP(e, t, cache)) {
                return true;
            }
        }
        return false;
    }
} 
