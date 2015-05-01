import java.util.*;
import java.io.*;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.graph.Node;
// java -cp ".:/home/montoya/fedra/apache-jena-2.11.0/lib/*" generateSomeRandomViews http://172.16.8.1:9373/sparql /home/montoya/dbpediaQueriesInstantions2012/ /home/montoya/dbpediaSetup/viewsSize1 1
class instantiateSomeQueriesVirtuoso {

    public static void main (String args[]) throws Exception {

        String endpoint = args[0];
        String templatesFolder = args[1];
        int number = Integer.parseInt(args[2]);
        String queriesFile = args[3];
        String predicatesFile = args[4];
        HashSet<String> preds = loadPredicates(predicatesFile);
        System.out.println("predicate list: "+preds);
        getQueries(endpoint, templatesFolder, number, queriesFile, preds);
    }

    public static HashSet<String> loadPredicates(String file) throws Exception {

        BufferedReader br = new BufferedReader(new FileReader(file));
        HashSet<String> ps = new HashSet<String>();
        String s = br.readLine();
        while(s != null) {
            if (s.startsWith("<")) {
                ps.add(s);
            }
            s = br.readLine();
        }
        br.close(); 
        return ps;
    }

    public static void getQueries (String endpoint, String templatesFolder, int number, String queriesFile, HashSet<String> preds) throws Exception {

        File f = new File(templatesFolder);
        File[] content = f.listFiles();
        HashMap<String, Query> hs = new HashMap<String, Query>();
        if (content != null) {
            Random r = new Random();
            int j = 0;
            for (File g : content) {
                    String path = g.getAbsolutePath();
                    Vector<String[]> triples = getTriples(path);
                    int i = 0;
                    HashSet<Vector<String[]>> vs = new HashSet<Vector<String[]>>();
                    HashMap<String, Query> views = new HashMap<String, Query>();
                    //int n = 100;
                    int l = 0;
                    do {
                    int n = 100;
                    boolean newView = false;
                    int constantTriple = r.nextInt(triples.size());
                    boolean instantiateSubject = r.nextBoolean();
                    String var = null;
                    if (instantiateSubject || preds.contains(triples.get(constantTriple)[1])) {
                        var = triples.get(constantTriple)[0];
                    } else {
                        var = triples.get(constantTriple)[2];
                    }
                    String body = getBody(triples);
                    int c = executeCount(endpoint, var, body);
                    n = Math.min(c, n);
                    System.out.println("generating "+n+" queries for template "+path);
                    HashSet<Integer> randomNumbers = new HashSet<Integer>();
                    if (c > n) {
                    while (randomNumbers.size() < n) {
                        int k = r.nextInt(c);
                        randomNumbers.add(k);
                    }
                    } else {
                    for (int k = 0; k < c; k++) {
                        randomNumbers.add(k);
                    }
                    }
                    for (int rn : randomNumbers) {
                        Vector<String[]> ts = instantiateVariable(triples, var, body, endpoint, rn);
                        Query v = QueryFactory.create(getSelectQuery(ts));
                        String viewName = startFederation.findName(views,v);
                        if (viewName==null) {
                            viewName = "query"+j;
                            views.put(viewName, v);
                            storeView(v, queriesFile);
                            i++;j++;
                            newView = true;
                        }
                    }
                    if (!newView) {
                        l++;
                    } else {
                        l = 0;
                    }
                    } while (i < number && l < 5);
                
            }
        }
    }

    public static Vector<String[]> instantiateVariable(Vector<String[]> triples, String var, String body, String endpoint, int r) {

        Vector<String[]> candidateQuery = new Vector<String[]>();
        String value = executeGetOneValue(endpoint, body, var, r); 
        for (String[] t : triples) {
            String s = t[0];
            String p = t[1];
            String o = t[2];
            if (s.equals(var)) {
                s = value;
            }
            if (o.equals(var)) {
                o = value;
            }
            String[] nt = {s, p, o};
            candidateQuery.add(nt);
        }
        return candidateQuery;
    }

    public static Vector<String[]> getTriples(String file) {
        Vector<String[]> triples = new Vector<String[]>();
        ConjunctiveQuery cq = new ConjunctiveQuery(file);
        List<Triple> ts = cq.getBody();
        for (Triple t : ts) {
            String[] components = {getString(t.getSubject()), getString(t.getPredicate()), getString(t.getObject())};
            triples.add(components);
        }
        return triples;
    }

    public static String getSelectQuery(Vector<String[]> triples) {
        String body = "";
        HashSet<String> vars = new HashSet<String>();
        for (String[] t : triples) {
            body += "  "+t[0]+"  "+t[1]+"  "+t[2]+" . ";
            double d = Math.random();
            if (t[0].startsWith("?") && d < 0.5) {
                vars.add(t[0]);
            }
            d = Math.random();
            if (t[2].startsWith("?") && d < 0.5) { 
                vars.add(t[2]);
            }
        }
        String varsStr ="";
        if (vars.size()==0) {
            varsStr = "*";
        } else {
            for (String v : vars) {
                varsStr += v + " ";
            }
        }
        return "SELECT DISTINCT "+varsStr+" WHERE { "+body+" } ";
    }

    public static String getBody(Vector<String[]> triples) {

        String body = "";
        for (String[] t : triples) {
            body += "  "+t[0]+"  "+t[1]+"  "+t[2]+" .\n";
        }
        return body;
    }

    public static void storeView(Query v, String viewsFile) throws Exception {

        BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
                                new FileOutputStream(viewsFile, true), "UTF-8"));
        String s = v.toString();
        s = s.replace('\n', ' ');
        output.write(s);
        output.newLine();
        output.flush();
        output.close(); 
    }

    public static String executeGetOneValue(String endpoint, String body, String var, int offset) {
        String query = "SELECT DISTINCT "+var+" \n WHERE { "+body;
        query += "} OFFSET "+offset+" LIMIT 1";
        //System.out.println("query in GetOne:\n"+query);
        QueryEngineHTTP queryExec = new QueryEngineHTTP(endpoint, query);
        ResultSet rs = queryExec.execSelect();
        String p = null;

        if (rs.hasNext()) {
            QuerySolution binding = rs.nextSolution();
            RDFNode n = binding.get(var);          
            p = getString(n);
        }
        queryExec.close();
        return p;
    }

    public static String getString(RDFNode n) {
            String p = "";
            if (n.isResource()) {
                p = "<"+n.asResource().toString()+">";
            } else if (n.isLiteral()) {
                Literal l = n.asLiteral();
                p = "\""+l.getLexicalForm()+"\"";
                String dt = l.getDatatypeURI();
                String lg = l.getLanguage();
                if (lg != null && !lg.equals("")) {
                    p = p + "@"+lg;
                }
                if (dt != null && !dt.equals("")) {
                    p = p + "^^<" + dt+">";
                }
            }
            return p;
    }

    public static String getString(Node n) {
            String p = "";
            if (n.isVariable()) {
                p="?"+n.getName();
            } else if (n.isURI()) {
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
            }
            return p;
    }

    public static int executeCount(String endpoint, String var, String body) {
        String query = "SELECT (COUNT(DISTINCT("+var+")) AS ?c) \n WHERE { "+body;
        query += "}";
        //System.out.println(query);
        QueryEngineHTTP queryExec = new QueryEngineHTTP(endpoint, query);
        ResultSet rs = queryExec.execSelect();
        int c = 0;
        if (rs.hasNext()) {
            QuerySolution binding = rs.nextSolution();
            RDFNode n = binding.get("?c");
            c = n.asLiteral().getInt();
        }
        queryExec.close();
        return c;
    }
}
