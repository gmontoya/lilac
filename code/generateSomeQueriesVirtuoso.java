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

class generateSomeQueriesVirtuoso {

    public static List<String> getPredicates(String file) throws Exception {

        BufferedReader br = new BufferedReader(new FileReader(file));
        List<String> ps = new ArrayList<String>();
        String s = br.readLine();
        br.close();
        StringTokenizer st = new StringTokenizer(s);
        String t = st.nextToken();
        while(st.hasMoreTokens()) {
            t = st.nextToken();
            if (t.startsWith("<")) {
                ps.add(t);
            }
        }
        return ps;
    }

    static boolean instantiate = false;

    public static void main (String args[]) throws Exception {

        String endpoint = args[0];
        String viewsFolder = args[1];
        String endpointsDescriptionFile = args[2];
        int size = Integer.parseInt(args[3]);
        int number = Integer.parseInt(args[4]);
        String shape = args[5];
        String nextFile = args[6];
        String withFile = args[7];
        instantiate = Boolean.parseBoolean(args[8]);

        List<String> ps = getPredicates(endpointsDescriptionFile);
        getViews(endpoint, viewsFolder, size, ps, number, shape, nextFile, withFile);
    }

    public static void load(String file, Vector<String> preds1, Vector<Vector<String>> preds2) throws Exception {

        BufferedReader br = new BufferedReader(new FileReader(file));
        String s = br.readLine();
        while (s != null) {
            StringTokenizer st = new StringTokenizer(s);
            String predicate1 = st.nextToken();
            Vector<String> preds = new Vector<String>();
            while (st.hasMoreTokens()) {
                String predicate2 = st.nextToken();
                preds.add(predicate2);
            }
            if (preds.size()>0) {
                preds1.add(predicate1);
                preds2.add(preds);
            }
            s = br.readLine();
        }
        br.close();
    }

    public static void getViews (String endpoint, String viewsFolder, int size, List<String> ps, int number, String shape, String nextFile, String withFile) throws Exception {

        int i = 0;
        HashSet<Vector<String[]>> vs = new HashSet<Vector<String[]>>();
        HashMap<String, Query> views = new HashMap<String, Query>();
        Vector<String> nextPreds1 = new Vector<String>();
        Vector<Vector<String>> nextPreds2 = new Vector<Vector<String>>();
        Vector<String> withPreds1 = new Vector<String>();
        Vector<Vector<String>> withPreds2 = new Vector<Vector<String>>();
        load(nextFile, nextPreds1, nextPreds2);
        load(withFile, withPreds1, withPreds2);
        Random r = new Random();
        //System.out.println("nextPreds1.size() "+nextPreds1.size());
        while (i < number) {
            constantTriple = r.nextInt(size);
            System.out.println("triple pattern to instantiate: "+constantTriple);
            String p = getOnePredicate(nextPreds1, withPreds1, shape);
            //System.out.println("Considering predicate "+p);
            Vector<String[]> candidateView = growQuery(p, endpoint, size, shape, nextPreds1, nextPreds2, withPreds1, withPreds2);
            if (candidateView != null) {
                Query v = QueryFactory.create(getSelectQuery(candidateView));
                String viewName = startFederation.findName(views,v);
                if (viewName==null) {
                    viewName = "fragment"+i;
                    views.put(viewName, v);
                    storeView(v, viewName, viewsFolder);
                    i++;
                }
            }
        }
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

    public static boolean covers(Vector<String[]> v, Vector<String[]> view) {

        for (String[] t : view) {
            boolean covered = false;
            for (String[] ti : v) {
                if (t[1].equals(ti[1]) && (ti[0].startsWith("?") || ti[0].equals(t[0])) && (ti[2].startsWith("?") || ti[2].equals(t[2]))) {
                    covered = true;
                }
            } 
            if (!covered) {
                return false;
            }
        }
        return true;
    }

    public static boolean newView(Vector<String[]> view, HashSet<Vector<String[]>> vs) {
    
        for (Vector<String[]> v : vs) {
            if (covers(v, view)) {
                return false;
            }
        }
        return true;
    }

    public static String getOnePredicate(Vector<String> nextPreds1, Vector<String> withPreds1, String shape) {
        double d = Math.random();
        if (nextPreds1.size()>0 && (shape.equals("PATH") || (!shape.equals("STAR")&&d<0.5))) {
            int i = (int) (Math.random()*nextPreds1.size());
            return nextPreds1.get(i);
        } else if (withPreds1.size()>0) {
            int i = (int) (Math.random()*withPreds1.size());
            return withPreds1.get(i);
        } else {
            return null;
        }
    }

    public static Vector<String[]> growQuery(String pred, String endpoint, int size, String shape, Vector<String> nextPreds1, Vector<Vector<String>> nextPreds2, Vector<String> withPreds1, Vector<Vector<String>> withPreds2) {

        Vector<String[]> triples = new Vector<String[]>();
        String[] t = getTripleP(endpoint, "", "?x1", pred, "?x2", triples);
        triples.add(t);
        int last = growQueryRec(triples, endpoint, t[0], 2, size, shape, nextPreds1, nextPreds2, withPreds1, withPreds2);
        //String body = getBody(triples);
        if (last-1 == size) {
            return triples;
        }
        return null;
    }

    public static String getBody(Vector<String[]> triples) {

        String body = "";
        for (String[] t : triples) {
            body += "  "+t[0]+"  "+t[1]+"  "+t[2]+" .\n";
        }
        return body;
    }

    public static int growQueryRec(Vector<String[]> triples, String endpoint, String subject, int last, int size, String shape, Vector<String> nextPreds1, Vector<Vector<String>> nextPreds2, Vector<String> withPreds1, Vector<Vector<String>> withPreds2) {

        if (last - 1 >= size) {
            return last;
        }

        String query = getBody(triples);
        boolean ask = false;
        String s = subject;
        String lastPred = triples.get(triples.size()-1)[1];
        String p ="?p";
        if (shape.equals("PATH")) {
            s = triples.get(triples.size()-1)[2];
            int i = nextPreds1.indexOf(lastPred);
            if (i<0 || (!s.startsWith("?") && !s.startsWith("<"))) {
                return last;
            }
            Vector<String> ps = nextPreds2.get(i);
            i = (int) (Math.random()*ps.size());
            p = ps.get(i);
        } else {
            int j = withPreds1.indexOf(lastPred);
            if (j<0) {
                return last;
            } 
            Vector<String> ps = new Vector(withPreds2.get(j));
            for (int i = 0; i < triples.size(); i++) {
                ps.remove(triples.get(i)[1]);
            }
            j = (int) (Math.random()*ps.size());
            if (j < ps.size()) {
                p = ps.get(j);
            } else {
                return last;
            }
        }
        if (shape.equals("RANDOM")) {
            double d = Math.random();
            if (d < 0.3) {
                for (int i = 0; i < triples.size(); i++) {
                    //int i = triples.size()-1;
                    String c = triples.get(i)[2];
                    d = Math.random();            
                    if (d < 0.5 && c.startsWith("?")) {
                        s = c;
                        d = Math.random();
                        lastPred = triples.get(i)[1];
                        int j = nextPreds1.indexOf(lastPred);
                        if (j<0) {
                            return last;
                        }
                        Vector<String> ps = nextPreds2.get(j);
                        j = (int) (Math.random()*ps.size());
                        p = ps.get(j);
                        break;
                    }
                }
            }
        }

        String queryTmp = query + "  "+s+" "+p+" ?x"+(last+1)+" . ";
        //String filter = getFilter(triples, "?p");
        System.out.println("so far, generated view: \n"+queryTmp);
        ask = makeBigAsk(endpoint, queryTmp);
        if (ask) {
            String[] t = getTripleP(endpoint, query, s, p, "?x"+(last+1), triples);
            triples.add(t);
            return growQueryRec(triples, endpoint, subject, last+1, size, shape, nextPreds1, nextPreds2, withPreds1, withPreds2);
        }
        return last;
    }

    static int constantTriple = 0;

    public static String[] getTripleP (String endpoint, String queryBefore, String subj, String pred, String obj, Vector<String[]> triples) {

        String p = pred;
        if (pred.startsWith("?")) {
            String query = queryBefore + " " + subj + " " + pred + " " + obj + " . ";
            if (triples.size()>0) {
                query = query + getFilter(triples, pred);
            }
            //double d = Math.random();
            p = executeGetOneValue(endpoint, query, pred);
        }
        String s = subj;
        boolean constantSubject = true;
        if (subj.startsWith("?")) {
            constantSubject = false;
            if (triples.size()==0) {
                String query = queryBefore + " " + subj + " " + p + " " + obj + " . ";
                int c = executeCount(endpoint, subj, query);
                double d = Math.random();
                if (c > 0 && (d < 0.5) && (triples.size()==constantTriple) && instantiate) {
                    s = executeGetOneValue(endpoint, query, subj);
                    constantSubject = true;
                }
            }
        }
        String o = obj;
        if (obj.startsWith("?")) {
            
            String query = queryBefore + " " + s + " " + p + " " + obj + " . ";
            int c = executeCount(endpoint, obj, query);
            //double d = Math.random();
            if (c > 0 && !constantSubject && (triples.size()==constantTriple) && instantiate) { // 1.0/c
                o = executeGetOneValue(endpoint, query, obj);
            }
        }
        String[] t = {s, p, o};
        return t;
    }

    public static String getFilter(Vector<String[]> triples, String var) {

     if (triples != null && triples.size()>0) {
        String[] t = triples.get(0);
        String f = "FILTER ("+var+" != "+t[1];
        for (int i = 1; i < triples.size(); i++) {
            t = triples.get(i);
            f += " && "+var+" != "+t[1];
        }

        f += ")\n";
        return f;
     }
     return "";
    }

    public static void storeView(Query v, String viewName, String viewsFolder) throws Exception {

        String newName = viewsFolder+"/"+viewName;
        BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
                                new FileOutputStream(newName), "UTF-8"));
        String s = v.toString();
        output.write(s);
        output.newLine();
        output.flush();
        output.close(); 
    }

    public static String executeGetOneValue(String endpoint, String body, String var) {

        String query = "SELECT DISTINCT "+var+" \n WHERE { "+body;
        query += "} LIMIT 1000";
        System.out.println("query in GetOne:\n"+query);
        QueryEngineHTTP queryExec = new QueryEngineHTTP(endpoint, query);
        ResultSet rs = queryExec.execSelect();
        String p = null;

        while (rs.hasNext()) {
            QuerySolution binding = rs.nextSolution();
            double d = Math.random();
            RDFNode n = binding.get(var);          
            p = getString(n);
            if (d >= 0.5) {
                queryExec.close();
                return p;
            }
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

    public static boolean makeBigAsk(String endpoint, String body) {

        String query = "ASK { "+body;
        query += "}";
        QueryEngineHTTP queryExec = new QueryEngineHTTP(endpoint, query);
        boolean b = queryExec.execAsk();
        queryExec.close();
        return b;
    }

    public static int executeCount(String endpoint, String var, String body) {

        String query = "SELECT DISTINCT (COUNT ("+var+") AS ?c) \n WHERE { "+body;
        query += "}";
        QueryEngineHTTP queryExec = new QueryEngineHTTP(endpoint, query);
        ResultSet rs = queryExec.execSelect();

        while (rs.hasNext()) {
            QuerySolution binding = rs.nextSolution();
            RDFNode n = binding.get("?c");
            int c = n.asLiteral().getInt();
            queryExec.close();
            return c;
        }
        queryExec.close();
        return 0;
    }
}
