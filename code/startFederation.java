/**
 * startFederation class takes as input the index of the fedra Federation
 * and process it, computing the "real sources" (do not take data from others),
 * "real origin" (from where the data really comes), and containments among views.
 * It produces as output the files that Fedra algorithm needs to perform the
 * source selection, it can be executed once to execute Fedra algorithm multiple
 * times for different queries. It needs to be re-executed when the federation 
 * members change.
 *
 * @author Gabriela Montoya
 * 
 */

import java.io.*;
import java.util.*;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.algebra.*;
import com.hp.hpl.jena.sparql.syntax.*; 
import com.hp.hpl.jena.graph.Triple;
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

class startFederation {

/*
    public static void main(String[] args) throws Exception {
        Query v1 = QueryFactory.read(args[0]);
        Query v2 = QueryFactory.read(args[1]);
        System.out.println(containedIn(v1, v2));
        System.out.println(v1.equals(v2));
    }*/

    public static void main(String[] args) throws Exception {

        String index = args[0];
        String endpointDescription = args[1]; // Url v14 v15 v16 v17
        String publicEndpointsFile = args[2]; // Endpoint that shouldn't be contacted
        String viewsContainment = args[3]; // vx vy ; vx is contained in vy
        String endpointsContainment = args[4]; // E10 v14 E3 E2 data for view v14 in E10 comes from E3 and E2
        String viewsDefinitionFolder = args[5];
        String datesFile = args[6];
        String divergenceFile = args[7];
        String lastDate = args[8];
        Model indexInfo = FileManager.get().loadModel(index);
        HashMap<String, Query> views = new HashMap<String,Query>();
        HashMap<String, HashSet<String>> endpoints = new HashMap<String, HashSet<String>>();
        HashSet<String> publicEndpoints = new HashSet<String>();
        HashMap<String,HashSet<String>> containedInView = new HashMap<String,HashSet<String>>();
        HashMap<String, HashMap<String,HashSet<String>>> containedInEndpoint 
                             = new HashMap<String, HashMap<String,HashSet<String>>>();
        HashMap<String,HashMap<String,HashMap<String,String>>> dates 
                             = new HashMap<String,HashMap<String,HashMap<String,String>>>();
        HashMap<String, HashMap<String, HashMap<String, Long>>> divergence = new HashMap<String, HashMap<String, HashMap<String, Long>>>();        
        loadData(indexInfo, views, endpoints, containedInEndpoint, publicEndpoints, dates, divergence, lastDate);
        //System.out.println("views: "+views);
        //System.out.println("endpoints: "+endpoints);
        //System.out.println("origin: "+origin);
        //System.out.println("dates: "+dates);
        //long t1 = System.currentTimeMillis();
        //processOrigin(origin, endpoints, views);
        //t1 = System.currentTimeMillis() - t1;
        //getSources(origin, publicEndpoints);
        long t1 = System.currentTimeMillis();
        getTransitiveClosure(containedInEndpoint);
        getQuasiTransitiveClosure(dates);
        t1 = System.currentTimeMillis() - t1;
        long t2 = System.currentTimeMillis();
        buildContainment(views, containedInView);
        t2 = System.currentTimeMillis() - t2;
        //System.out.println("ci:" +containedIn);

        storeEndpointsDescription(endpoints, endpointDescription);
        storeEndpoints(publicEndpoints, publicEndpointsFile);
        storeContainment(containedInView, viewsContainment);
        storeViewsDefinition(views, viewsDefinitionFolder);
        storeDates(dates, datesFile);
        storeDivergence(divergence, divergenceFile);
        storeContainmentEndpoint(containedInEndpoint, endpointsContainment);
        //"real origin computation time" "containment computation time"
        System.out.println(t1+"\t"+t2);
    }

    public static void storeContainmentEndpoint(HashMap<String, HashMap<String,HashSet<String>>> containedInEndpoint, String file) throws Exception {


        BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
                                    new FileOutputStream(file), "UTF-8"));
        for (String endpoint1 : containedInEndpoint.keySet()) {
            HashMap<String,HashSet<String>> viewAndSources = containedInEndpoint.get(endpoint1);
            for (String view : viewAndSources.keySet()) {
                HashSet<String> sources = viewAndSources.get(view);
                    output.write(endpoint1+" "+view+" ");
                for (String s : sources) {
                    output.write(s+" ");
                }
                output.write("\n");
            }
        }
        output.flush();
        output.close();
    }

    public static void getQuasiTransitiveClosure(HashMap<String,HashMap<String,HashMap<String,String>>> dates) {

        boolean fixPoint = false;
        while (!fixPoint) {
            fixPoint = true;
            loop: for (String e1 : dates.keySet()) {
                //System.out.println("e1: "+e1);
                HashMap<String,HashMap<String,String>> viewSourceDate = dates.get(e1);
                for (String view : viewSourceDate.keySet()) {
                    //System.out.println("view: "+view);
                    HashMap<String,String> sourceDate = viewSourceDate.get(view);
                    for (String s : sourceDate.keySet()) {
                        //System.out.println("source: "+s);
                        if (dates.get(s) != null && dates.get(s).get(view) != null) {
                            //System.out.println("xx");
                            HashMap<String,String> transitiveSources = dates.get(s).get(view);
                            HashMap<String,String> newSources = new HashMap<String,String>();
                            newSources.putAll(sourceDate);
                            for (String ts : transitiveSources.keySet()) {
                                String tsDate = transitiveSources.get(ts);
                                String date = sourceDate.get(s);
                                if (tsDate.compareTo(date)<0) {
                                    date = tsDate;
                                }
                                newSources.put(ts,date);
                            }
                            newSources.remove(e1);
                            if (!newSources.equals(sourceDate)) {
                                //System.out.println(newSources.size()+">"+size);
                                viewSourceDate.put(view, newSources);
                                dates.put(e1, viewSourceDate);
                                fixPoint = false;
                                break loop;
                            }
                        }
                    }
                }
            }
        }
    }

    public static void getTransitiveClosure(HashMap<String, HashMap<String,HashSet<String>>> containedInEndpoint) {

        boolean fixPoint = false;
        while (!fixPoint) {
            fixPoint = true;
            loop: for (String e1 : containedInEndpoint.keySet()) {
                //System.out.println("e1: "+e1);
                HashMap<String,HashSet<String>> viewSources = containedInEndpoint.get(e1);
                for (String view : viewSources.keySet()) {
                    //System.out.println("view: "+view);
                    HashSet<String> sources = viewSources.get(view);
                    for (String s : sources) {
                        //System.out.println("source: "+s);
                        if (containedInEndpoint.get(s) != null && containedInEndpoint.get(s).get(view) != null) {
                            //System.out.println("xx");
                            HashSet<String> transitiveSources = containedInEndpoint.get(s).get(view);
                            HashSet<String> newSources = new HashSet<String>();
                            newSources.addAll(transitiveSources);
                            int size = sources.size();
                            newSources.addAll(sources);
                            newSources.remove(e1);
                            if (newSources.size()>size) {
                                //System.out.println(newSources.size()+">"+size);
                                viewSources.put(view, newSources);
                                containedInEndpoint.put(e1, viewSources);
                                fixPoint = false;
                                break loop;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Loads the data from the index into HashMap structures
     *
     * @param indexInfo the federation information as a model.
     * @param views hashmap that links the view names with the view definitions.
     * @param endpoints hashmap that relates each endpoint with the view names that
     *                  it exposes.
     * @param origin hashmap that relates for each endpoint and each view exposed
     *               by it, the set of endpoints that provide the data for them.
     * @param dates hashmap that relates for each endpoint, each view exposed by
     *              it and each origin of the view data, the date when the data 
     *              was taken from its origin.
     */
    public static void loadData (Model indexInfo, HashMap<String, Query> views, 
                                 HashMap<String, HashSet<String>> endpoints, 
                                 HashMap<String, HashMap<String,HashSet<String>>> containedInEndpoint, 
                                 HashSet<String> publicEndpoints, 
                                 HashMap<String,HashMap<String,HashMap<String,String>>> dates,
                                 HashMap<String, HashMap<String, HashMap<String, Long>>> divergence, 
                                 String lastDate) throws Exception {

        String pref = "PREFIX sd: <http://www.w3.org/ns/sparql-service-description#> \n"
                     +" PREFIX dc: <http://purl.org/dc/elements/1.1/> \n"
                     +" PREFIX dcterms: <http://purl.org/dc/terms/> \n ";
        Query q = QueryFactory.create(pref+"SELECT DISTINCT ?u ?f \n"
                 +" WHERE { ?s a sd:Service . ?s sd:endpoint ?u . ?s dcterms:hasPart ?f }");
        QueryExecution queryExec = QueryExecutionFactory.create(q.toString(), indexInfo);
        ResultSet rs = queryExec.execSelect();
        int i = 1;
        HashSet<String> privateEndpoints = new HashSet<String>();
        HashSet<String> possiblePublicEndpoints = new HashSet<String>();
        while (rs.hasNext()) {
            QuerySolution solution = rs.nextSolution();
            // endpoint url
            RDFNode n = solution.get("?u");
            String url = n.toString();
            privateEndpoints.add(url);
            // fragment exposed by the endpoint
            RDFNode n2 = solution.get("?f");
            if (n2 instanceof ResourceImpl) {
                ResourceImpl ri = (ResourceImpl) n2;
                Property p = indexInfo.createProperty("http://purl.org/dc/elements/1.1/", 
                                                      "description");
                Statement s = ri.getProperty(p);
                // fragment definition
                String viewDefinition = s.getString();
                Query v = QueryFactory.create(viewDefinition);
                String viewName = findName(views, v);
                if (viewName == null) {
                    viewName = "view"+i;
                    i++;
                    views.put(viewName, v);
                }
                HashSet<String> fs = endpoints.get(url);
                if (fs == null) {
                    fs =  new HashSet<String>();
                }
                fs.add(viewName);
                endpoints.put(url, fs);
                // fragment origin
                p = indexInfo.createProperty("http://purl.org/dc/elements/1.1/", "source");
                boolean complete = true;
                if (ri.hasProperty(p)) {
                    s = ri.getProperty(p);
                    complete = false;
                } else {
                    p = indexInfo.createProperty("http://purl.org/dc/terms/", "source");
                    s = ri.getProperty(p);
                }
                String o = s.getResource().toString();
                possiblePublicEndpoints.add(o);
                fs = endpoints.get(o);
                if (fs == null) {
                    fs =  new HashSet<String>();
                }
                fs.add(viewName);
                endpoints.put(o, fs);

                HashMap<String,HashSet<String>> viewSources = containedInEndpoint.get(url);
                if (viewSources == null) {
                    viewSources = new HashMap<String,HashSet<String>>();
                }
                HashSet<String> sources = viewSources.get(viewName);
                if (sources == null) {
                    sources = new HashSet<String>();
                    sources.add(o);
                    viewSources.put(viewName, sources);
                } else {
                    viewSources.remove(viewName);
                    if (viewSources.size()==0) {
                        containedInEndpoint.remove(url);
                    }
                }
                if (viewSources.size()>0) {
                    containedInEndpoint.put(url, viewSources);
                }
                if (complete) {
                    viewSources = containedInEndpoint.get(o);
                    if (viewSources == null) {
                        viewSources = new HashMap<String,HashSet<String>>();
                    }
                    sources = viewSources.get(viewName);
                    if (sources == null) {
                        sources = new HashSet<String>();
                    }
                    sources.add(url);
                    viewSources.put(viewName, sources);
                    containedInEndpoint.put(o, viewSources);
                }

                p = indexInfo.createProperty("http://purl.org/dc/elements/1.1/", "date");
                s = ri.getProperty(p);
                String date = s.getString();
                HashMap<String, HashMap<String, String>> perEndpointDates = dates.get(url);
                if (perEndpointDates == null) {
                    perEndpointDates = new HashMap<String,HashMap<String,String>>();
                }                
                HashMap<String, String> perEndpointViewDates = perEndpointDates.get(viewName);
                if (perEndpointViewDates == null) {
                    perEndpointViewDates = new HashMap<String, String>();
                }
                perEndpointViewDates.put(o, date);
                perEndpointDates.put(viewName, perEndpointViewDates);
                dates.put(url, perEndpointDates);

                HashMap<String, HashMap<String, Long>> perView = divergence.get(viewName);
                if (perView == null) {
                     perView = new HashMap<String, HashMap<String, Long>>();
                }
                HashMap<String, Long> perEndpointView = perView.get(o);
                if (perEndpointView == null) {
                     perEndpointView = new HashMap<String, Long>();
                }
                long div = getDivergence(date, lastDate);
                perEndpointView.put(date, div);
                perView.put(o, perEndpointView);
                divergence.put(viewName, perView);
            }
        }
        publicEndpoints.addAll(possiblePublicEndpoints);
        publicEndpoints.removeAll(privateEndpoints);
    }

    public static long getDivergence(String date, String lastDate) {

        String [] dateParts = date.split("-");
        int year = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]);
        int day = Integer.parseInt(dateParts[2]);
        int hour = Integer.parseInt(dateParts[3]);
        int counter = Integer.parseInt(dateParts[4]);
        dateParts = lastDate.split("-");
        int yearLD = Integer.parseInt(dateParts[0]);
        int monthLD = Integer.parseInt(dateParts[1]);
        int dayLD = Integer.parseInt(dateParts[2]);
        int hourLD = Integer.parseInt(dateParts[3]);
        Calendar c = Calendar.getInstance();
        c.set(year, month, day, hour, 0);
        Calendar cLD = Calendar.getInstance();
        cLD.set(yearLD, monthLD, dayLD, hourLD, 0);
        long div = cLD.getTimeInMillis() - c.getTimeInMillis();
        return div;
    }

    /**
     * Check if query definition p provides a complete
     * fragment of query definition q. A fragment p is complete
     * with respect to q if all the predicates present in p, 
     * are also present in q.
     * Ex: p :- p1, p2, p3 it is complete for q1 :- p1, p2, p3, p4
     * but not for q2 :- p1, p2. because the elements that do not
     * satisfy p3 are missing.
     * 
     * @param p possible complete (sub) part
     * @param q possible composed query
     */
    public static boolean providesAPart(Query p, Query v) {
        
        HashSet<String> predsP = getPreds(p);
        HashSet<String> predsV = getPreds(v);
        HashSet<String> preds = new HashSet<String>();
        preds.addAll(predsP);
        preds.retainAll(predsV);
        // all the predicates present in p are also present in v
        boolean b = preds.equals(predsP);
        ConjunctiveQuery cqP = new ConjunctiveQuery(p, "cqp");
        ConjunctiveQuery cqV = new ConjunctiveQuery(v, "cqv");
        List<Triple> tsP = cqP.getBody();
        List<Triple> tsV = cqV.getBody();
        Collection<HashSet<Pair<Node, String>>> ctp = getJoins(tsP);
        Collection<HashSet<Pair<Node, String>>> ctv = getJoins(tsV);
        // each join restriction in p is also a restriction in v
        for (HashSet<Pair<Node, String>> s : ctp) {
            b = b && coveredBy(s, ctv);
        }
        
        return b;
    }

    /**
     * Check if a set of query definitions, such that each of them provides
     * a complete part of v, actually provide all the predicates 
     * present in v.
     *
     * @param ps set of query definitions that each cover a complete part of v
     * @param v query definition
     */
    public static boolean providesAllParts(HashSet<Query> ps, Query v) {
        HashSet<String> predsPs = new HashSet<String>();
        HashSet<String> predsV = getPreds(v);
        for (Query p : ps) {
            predsPs.addAll(getPreds(p));
        }
        HashSet<String> tmp = new HashSet<String>();
        tmp.addAll(predsV);
        tmp.retainAll(predsPs);
        return tmp.equals(predsV);
    }

    public static String findName(HashMap<String,Query> views, Query v) {

        for (String n : views.keySet()) {
            Query q = views.get(n);
            if (containedIn(q, v) && containedIn(v, q)) {
                return n;
            }
        }
        return null;
    }

    /**
     * Given two queries p and v, check if p is contained in v, when v
     * includes a subset of the predicates included in p. The join 
     * restrictions imposed to v should be also imposed to p.
     * Ex: p :- ?x p1 ?y, ?y p2 ?z, ?z p3 ?w is contained in 
     *    q1 :- ?a p2 ?b, ?b p3 ?c but not in q2 :- ?a p1 ?b, ?a p2 ?c
     *    nor in q3 :- ?a p1 ?b, ?b p2 ?c, ?c p4 ?d.
     * Currently works for views with bounded predicates and unbounded
     * subjects and objects. It may be extended to remove these restrictions
     * but then a more complex algorithm is required.
     * 
     * @param p query to check if it contained
     * @param v query to check if contains p
     */
    public static boolean containedIn(Query p, Query v) {
        ConjunctiveQuery cqP = new ConjunctiveQuery(p, "cqp");
        ConjunctiveQuery cqV = new ConjunctiveQuery(v, "cqv");
        List<Triple> tsP = cqP.getBody();
        List<Triple> tsV = cqV.getBody();
        Collection<HashSet<Pair<Node, String>>> ctp = getJoins(tsP);
        Collection<HashSet<Pair<Node, String>>> ctv = getJoins(tsV);
        HashMap<Node,HashSet<Pair<Node, String>>> ctcp = getConstants(tsP);
        HashMap<Node,HashSet<Pair<Node, String>>> ctcv = getConstants(tsV);
        boolean b = getPreds(p).containsAll(getPreds(v));
        for (HashSet<Pair<Node, String>> s : ctv) {
            b = b && coveredBy(s, ctp, ctcp);
        }
        for (Node c : ctcv.keySet()) {
            HashSet<Pair<Node, String>> cV = ctcv.get(c);
            HashSet<Pair<Node, String>> cP = ctcp.get(c);
            b = b && (cP != null) && cP.containsAll(cV);
        }
        return b;
    }

    /**
     * Given two triple pattern, get the name of one variable that joins them.
     * Currently only considers variables in Subject or Object positions.
     *
     * @param t1 a triple pattern
     * @param t2 a triple pattern
     */
    public static String getJoinVar(Triple t1, Triple t2) {

        Node s = t1.getSubject();
        if (s.isVariable()) {
            if ((t2.getSubject().isVariable() && t2.getSubject().equals(s))
                || (t2.getObject().isVariable() && t2.getObject().equals(s))) {
                return s.getName();
            }
        }
        Node o = t1.getObject();
        if (o.isVariable()) {
            if ((t2.getSubject().isVariable() && t2.getSubject().equals(o)) 
                || (t2.getObject().isVariable() && t2.getObject().equals(o))) {
                return o.getName();
            }
        }
        return null;
    }

    /**
     * Given a variable name and a triple pattern, determine the position that
     * variable has in the triple pattern.
     * Currently only considers variables in Subject or Object positions.
     *
     * @param v a variable name
     * @param t a triple pattern
     */
    public static String getPosition(String v, Triple t) {

        Node s = t.getSubject();
        if (s.isVariable() && s.getName().equals(v)) {
            return "Subject";
        }
        Node o = t.getObject();
        if (o.isVariable() && o.getName().equals(v)) {
            return "Object";
        }
        return null;
    }

    /**
     * Given a list of triples, produces a collection of sets, where each set corresponds
     * to a variable present in the triple patterns and that make join between different
     * triples. The elements of the set are pair that have in fist position the predicate
     * of the triple that joins and in second position the "position" of the variable in 
     * triple (the position may be 'Subject' or 'Object'). This structure allows to 
     * abstract from the variable names.
     */
    public static Collection<HashSet<Pair<Node, String>>> getJoins(List<Triple> l) {

        HashMap<String,HashSet<Pair<Node, String>>> map 
                                      = new HashMap<String,HashSet<Pair<Node, String>>>();

        for (Triple t : l) {
            for (Triple u : l) {
                if (!t.equals(u)) {
                    String v = getJoinVar(t, u);
                    if (v != null) {
                        HashSet<Pair<Node, String>> perVar = map.get(v);
                        if (perVar == null) {
                            perVar = new HashSet<Pair<Node, String>>();
                        }
                        String pt = getPosition(v, t);
                        String pu = getPosition(v, u);
                        perVar.add(new Pair(t.getPredicate(), pt));
                        perVar.add(new Pair(u.getPredicate(), pu));
                        map.put(v, perVar);
                    }
                }
            }
        }
        Collection<HashSet<Pair<Node, String>>> c = map.values();
        return c;
    }

    /**
     * Given a list of triples, produces a map, where each key corresponds
     * to a subject or object constant present in the triple patterns, and its 
     * value is a set.
     * The elements of the set are pairs that have in fist position the predicate
     * of the triple and in second position the "position" of the constant in 
     * triple (the position may be 'Subject' or 'Object').
     */
    public static HashMap<Node,HashSet<Pair<Node, String>>> getConstants(List<Triple> l) {

        HashMap<Node,HashSet<Pair<Node, String>>> map
                                      = new HashMap<Node,HashSet<Pair<Node, String>>>();

        for (Triple t : l) {
            Node s = t.getSubject();
            Node o = t.getObject();
            if (s.isConcrete()) {
                addPair(map, s, t.getPredicate(), "Subject");
            }
            if (o.isConcrete()) {
                addPair(map, o, t.getPredicate(), "Object");
            }
        }
        return map;
    }

    private static void addPair(HashMap<Node,HashSet<Pair<Node, String>>> map, Node n, Node p, String pos) {

        HashSet<Pair<Node, String>> perVar = map.get(n);
        if (perVar == null) {
            perVar = new HashSet<Pair<Node, String>>();
        }
        perVar.add(new Pair(p, pos));
        map.put(n, perVar); 
    }

    /**
     * Given the set of join restriction for one variable, and a collection of
     * join restrictions, check if there is one of the restriction in the
     * collection that covers the given one.
     *
     * @param s join restrictions for one variable
     * @param ss collection of join restrictions
     *
     */
    public static boolean coveredBy(HashSet<Pair<Node, String>> s, 
                                    Collection<HashSet<Pair<Node, String>>> ss,
                                    HashMap<Node,HashSet<Pair<Node, String>>> constants) {

        boolean b = false;
        for (HashSet<Pair<Node, String>> t : ss) {
            if (t.containsAll(s)) {
                return true;
            }
        }
        for (Node c : constants.keySet()) {
            HashSet<Pair<Node, String>> ct = constants.get(c);
            if (ct.containsAll(s)) {
                return true;
            }
        }
        return b;

    }

    public static boolean coveredBy(HashSet<Pair<Node, String>> s,
                                    Collection<HashSet<Pair<Node, String>>> ss) {
        return coveredBy(s,ss,new HashMap<Node,HashSet<Pair<Node, String>>>());
    }

    /**
     * Given a query, obtains a set of the predicates present in the triple
     * patterns that compose the query.
     * 
     * @param v the query
     * @return a set with the predicates present in v
     */
    public static HashSet<String> getPreds (Query v) {

        Op op = Algebra.compile(v);
        TriplesVisitor mv = new TriplesVisitor();
        OpWalker ow = new OpWalker();
        ow.walk(op, mv);
        List<Triple> ts = mv.getTriples();
        HashSet<String> ps = new HashSet<String>();
        for (Triple t : ts) {
            ps.add(t.getPredicate().toString());
        }
        return ps;
    }

    /**
     * Given a set of views and their origins, a view, and the map
     * that relates view names with view definitions, determine
     * the subset of those views that may provide the instantiation
     * of the view. The views that provide a part of the view should
     * provide the whole part.
     *
     * @param os maps the views with their origins
     * @param view view for which the relevant views should be found
     * @param views mapping from view names to view definitions
     * @return a set if views that provide the instantiation for view.
     */
    public static HashSet<String> getRelevants(HashMap<String, HashSet<String>> os, 
                                               String view, HashMap<String, Query> views) {

        HashSet<String> rs = new HashSet<String>();
        HashSet<Query> qs = new HashSet<Query>();
        for (String k : os.keySet()) {
            if (providesAPart(views.get(k), views.get(view))) {
                rs.add(k);
                qs.add(views.get(k));
            }
        }
        if (!providesAllParts(qs, views.get(view))) {
            return null;
        }
        return rs;
    }

    /**
     * Fills the origin information, for the "real sources", they are themselves the origin of
     * the exposed views. If some view is taken from an endpoint, then it should expose this
     * view too; and if it was not already declared, then some other views should have been
     * declared that provide the information for the undeclared view. Also, the "real origin"
     * is computed, then when two endpoints have the same "real origin" for the same view, 
     * it is known that they provide the same data, even if one of them did not take the 
     * data directly from the "real origin".
     *
     * @param origin initially contains the information given by the index that indicates from
     *               where each view instance was taken, and it completed for the sources, the
     *               implicitly provided views, and ajusted according to the transitive clausure.
     * @param endpoints maps between endpoints and the set of views that they provide.
     * @param views maps between view names and view definitions.
     */
    public static void processOrigin(HashMap<String, HashMap<String,HashSet<String>>> origin, 
                                     HashMap<String, HashSet<String>> endpoints, 
                                     HashMap<String, Query> views) {

        boolean fixPoint = false;
        /* First step: include in the sources the views that are taken from them, 
         *             and put themselves as origin
         */
        while (!fixPoint) {
          fixPoint = true;
          String oo = null;
          String vv = null;
          boolean b = false;
          // for each registred endpoint
          for (String url : origin.keySet()) {
            HashMap<String,HashSet<String>> perEndpoint = origin.get(url);
            // for each fragment in the endpoint
            for (String view : perEndpoint.keySet()) {
                HashSet<String> perEndpointView = perEndpoint.get(view);
                // for each recorded origin of the fragment
                for (String o : perEndpointView) {
                    HashSet<String> aux = new HashSet<String>();
                    aux.add(o);
                    if (origin.get(o) == null || 
                            (origin.get(o).containsValue(aux) 
                         && (origin.get(o).get(view) == null))) {
                        fixPoint = false;
                        oo = o;
                        vv = view;
                        b = true;
                        break;
                    }
                }
                if (b) {
                    break;
                }
            }
            if (b) {
                break;
            }
          }
          if (b) {
              // Look into the original source found
              HashMap<String, HashSet<String>> tmp = origin.get(oo);
              if (tmp == null) {
                  tmp = new HashMap<String, HashSet<String>>();
              }
              HashSet<String> tmp2 = new HashSet<String>();
              tmp2.add(oo);
              // include that it provides the concerned view and it's its own origin
              tmp.put(vv, tmp2);
              origin.put(oo, tmp);
              HashSet<String> tmp3 = endpoints.get(oo);
              if (tmp3 == null) {
                  tmp3 = new HashSet<String>();
              }
              tmp3.add(vv);
              // update information about the original endpoint
              endpoints.put(oo, tmp3);
          }
        }
        //System.out.println("first part ready");
        //System.out.println("origin: "+origin);
        fixPoint = false;
        boolean missingSource = false;
        String msg = "";
        /* second step: include views that are implicitly provided
         * Ex: endpoint A provides view v with data from B, but maybe B does not consider
         * v but w, and with w it also provides data for v
         */
        while(!fixPoint) {
            fixPoint = true;
            for (String url : origin.keySet()) {
                HashMap<String,HashSet<String>> perEndpoint = origin.get(url);
                for (String view : perEndpoint.keySet()) {
                    HashSet<String> perEndpointView = perEndpoint.get(view);
                    for (String o : perEndpointView) {
                        if (origin.get(o) != null && origin.get(o).get(view) == null) {
                            HashMap<String, HashSet<String>> os = origin.get(o);
                            HashSet<String> relevants = getRelevants(os, view, views);
                            if (relevants != null) {
                                fixPoint = false;
                                HashSet<String> ros = new HashSet<String>();
                                for (String r : relevants) {
                                    ros.addAll(os.get(r));
                                }
                                os.put(view, ros);
                                origin.put(o,os);
                                HashSet<String> tmp = endpoints.get(o);
                                if (tmp == null) {
                                    tmp = new HashSet<String>();
                                }
                                tmp.add(view);
                                endpoints.put(o, tmp);
                            } else { 
                                missingSource = true;
                                msg += "missing source for view "+view+" in "+url+".\n";
                            }
                        }
                    }
                }
            }
        }
        if (missingSource) {
            System.err.println(msg);
        }
        //System.out.println("second part ready");
        //System.out.println("origin: "+origin);
        // Third step: take the "transitive clausure" of the origin
        fixPoint = false;
        while(!fixPoint) {
            fixPoint = true;
            for (String url : origin.keySet()) {
                HashMap<String,HashSet<String>> perEndpoint = origin.get(url);
                for (String view : perEndpoint.keySet()) {
                    HashSet<String> perEndpointView = perEndpoint.get(view);
                    for (String o : perEndpointView) {
                        HashSet<String> aux = new HashSet<String>();
                        aux.add(o);
                        // when the origin endpoint, as another endpoint as origin of this view
                        if (origin.get(o) != null && origin.get(o).get(view) != null 
                               && !origin.get(o).get(view).equals(aux)) {
                            fixPoint = false;
                            perEndpointView.remove(o);
                            perEndpointView.addAll(origin.get(o).get(view));
                            perEndpoint.put(view, perEndpointView);
                            origin.put(url, perEndpoint);
                        }
                    }
                }
            }
        }
    }

    /**
     * Using the origin mapping, get the endpoints that do not have origin for its views
     * or that they are their own origin ("real sources")
     *
     * @param origin mapping where for each endpoint, and each of its views indicates the
     *               endpoints from which data have been taken.
     * @param publicEndpoint set of "real sources".
     */
    public static void getSources(HashMap<String, HashMap<String,HashSet<String>>> origin, 
                                  HashSet<String> publicEndpoint) {

        for (String url : origin.keySet()) {
            HashMap<String,HashSet<String>> perEndpoint = origin.get(url);
            for (String view : perEndpoint.keySet()) {
                HashSet<String> perEndpointView = perEndpoint.get(view);
                if (perEndpointView == null 
                     || (perEndpointView.size() == 1 && perEndpointView.contains(url))) {
                    publicEndpoint.add(url);
                }
            }
        }
    }

    /**
     * Given a set of views, search for definition containments among them.
     * 
     * @param views map between view names and view definitions
     * @param containedIn map that relates a view and the views that contain it.
     */
    public static void buildContainment(HashMap<String, Query> views, 
                                        HashMap<String,HashSet<String>> containedIn) {

        for (String k1 : views.keySet()) {
            Query q1 = views.get(k1);
            for (String k2 : views.keySet()) {
                Query q2 = views.get(k2);
                if (containedIn(q1, q2)) {
                    HashSet<String> sv= containedIn.get(k1);
                    if (sv == null) {
                        sv = new HashSet<String>();
                    }
                    sv.add(k2);
                    containedIn.put(k1, sv);
                }
            }
        }
    }

    /**
     * Given a map that relates the endpoints that views that it exposes, and a
     * file name, write the contents of the map in the file.
     *
     * @param endpoints the map between endpoints and its views
     * @param file the file name
     */
    public static void storeEndpointsDescription(HashMap<String, HashSet<String>> endpoints, 
                                                 String file) throws Exception {

        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                                                      new FileOutputStream(file), "UTF-8"));
        for (String e : endpoints.keySet()) {
            HashSet<String> vs = endpoints.get(e);
            out.write(e+" ");
            for (String v : vs) {
                out.write(v+" ");
            }
            out.write("\n");
        }
        out.flush();
        out.close();
    }

    /**
     * Given a set with the endpoint that are sources ("public endpoints"), and a
     * file name, write the contents of the map in the file.
     *
     * @param publicEndpoints set of source endpoints
     * @param file the file name
     */
    public static void storeEndpoints(HashSet<String> publicEndpoints, String file) 
                                                                     throws Exception {

        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                                                      new FileOutputStream(file), "UTF-8"));
        for (String e : publicEndpoints) {
            out.write(e+"\n");
        }
        out.flush();
        out.close();
    }

    /**
     * Given a map that for each view relates it to the set of views that contain it,
     * and a file name, write the contents of the map in the file. Each line will contain
     * one of the views that contains the view.
     *
     * @param containedIn the map between views and the views that contain it.
     * @param file the file name
     */
    public static void storeContainment(HashMap<String,HashSet<String>> containedIn, 
                                        String file) throws Exception {

        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                                                      new FileOutputStream(file), "UTF-8"));
        for (String v1 : containedIn.keySet()) {
            HashSet<String> sv = containedIn.get(v1);
            for (String v2 : sv) {
                out.write(v1+" "+v2+"\n");
            }
        }
        out.flush();
        out.close();
    }

    /**
     * Given a map that for each endpoint, and each view gives the set of endpoints
     * from where view instantiation has been taken, and a file name, write the contents 
     * of the map in the file.
     *
     * @param origin the map between endpoints, views, and their origins
     * @param file the file name
     */
    public static void storeOrigin(HashMap<String, HashMap<String,HashSet<String>>> origin, 
                                   String file) throws Exception {

        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                                                      new FileOutputStream(file), "UTF-8"));
        for (String e : origin.keySet()) {
            HashMap<String,HashSet<String>> perEndpoint = origin.get(e);
            for (String v : perEndpoint.keySet()) {
                HashSet<String> os = perEndpoint.get(v);
                out.write(e+" "+v+" ");
                for (String o : os) {
                    out.write(o+" ");
                }
                out.write("\n");
            }
        }
        out.flush();
        out.close();
    }

    /**
     * Given a map that relates view names with view definitions, and a folder path,
     * writes in the folder a file with a view definition for each view present in the map.
     *
     * @param views the map between view names and view definitions.
     * @param folder the folder path.
     */
    public static void storeViewsDefinition(HashMap<String, Query> views, 
                                            String folder) throws Exception {

        for (String name : views.keySet()) {
            Query definition = views.get(name);
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                                          new FileOutputStream(folder+"/"+name), "UTF-8"));
            out.write(definition.toString());
            out.flush();
            out.close();
        }
    }

    /**
     * Given a map that for each endpoint, each view gives, and each origin of these views,
     * the date when view instantiation has been taken, and a file name, write the contents 
     * of the map in the file.
     *
     * @param dates the map between endpoints, views, their origins, and dates
     * @param file the file name
     */
    public static void storeDates(HashMap<String, HashMap<String,HashMap<String, String>>> dates, 
                                  String file) throws Exception {

        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                                                      new FileOutputStream(file), "UTF-8"));
        for (String e : dates.keySet()) {
            HashMap<String,HashMap<String,String>> perEndpoint = dates.get(e);
            for (String v : perEndpoint.keySet()) {
                HashMap<String,String> perEndpointView = perEndpoint.get(v);
                for (String o : perEndpointView.keySet()) {
                    String d = perEndpointView.get(o);
                    out.write(e+" "+v+" "+o+" "+d+"\n");
                }
            }
        }
        out.flush();
        out.close();
    }

    public static void storeDivergence(HashMap<String, HashMap<String, HashMap<String, Long>>> divergence, String file) throws Exception {

        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
        for (String v : divergence.keySet()) {
            HashMap<String,HashMap<String,Long>> perView = divergence.get(v);
            for (String e : perView.keySet()) {
                HashMap<String,Long> perViewEndpoint = perView.get(e);
                for (String d : perViewEndpoint.keySet()) {
                    Long div = perViewEndpoint.get(d);
                    out.write(v+" "+e+" "+d+" "+div+"\n");
                }
            }
        }
        out.flush();
        out.close();
    }
}
