import java.util.*;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import java.io.*;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.Node;

/* Given a file with a triple pattern fragment construct query, the folder that contain all the
fragments construct queries, and a file with the endpoint mapping, i.e., a mapping that for
each fragment indicates the endpoints that have replicated it, returns the number
of times fragments that contain the given fragment have been replicated */

public class countRelevantFragments {

    public static void main(String [] args) throws Exception {
        String fragmentFile = args[0];
        Query fragment = QueryFactory.read(fragmentFile);
        String fragmentsFolder = args[1];
        String endpointsFile = args[2];
        HashMap<String,Query> fragments = updateFedraFiles.loadViews(fragmentsFolder);
        HashMap<String, HashSet<String>> endpoints = loadEndpointsFile(endpointsFile);
        HashSet<String> containedIn = findSuperViews(fragment, fragments);
        int n = countReplicas(containedIn, endpoints);
        System.out.println(n);
    }

    public static int countReplicas(HashSet<String> views, HashMap<String, HashSet<String>> endpoints) {
        int s = 0;
        for (String fragment : views) {
            HashSet<String> vs = endpoints.get(fragment);
            s = s + vs.size();
        }
        return s;
    }

    public static HashSet<String> findSuperViews(Query v, HashMap<String, Query> vs) {

        HashSet<String> svs = new HashSet<String>();
        ConjunctiveQuery cqV = new ConjunctiveQuery(v, "cqv");
        List<Triple> tsV = cqV.getBody();
        Triple t1 = tsV.get(0);
        Node s1 = t1.getSubject();
        Node p1 = t1.getPredicate();
        Node o1 = t1.getObject();
        for (String vn : vs.keySet()) {
            Query w = vs.get(vn);
            ConjunctiveQuery cqW = new ConjunctiveQuery(w, "cqw");                                                                
            List<Triple> tsW = cqW.getBody();                                                                                    
            Triple t2 = tsW.get(0);                                                                                        
            Node s2 = t2.getSubject();
            Node p2 = t2.getPredicate();
            Node o2 = t2.getObject();
            if (weaker(s2, s1) && weaker(p2, p1) && weaker(o2, o1)) {
                svs.add(vn);
            }
        }
        return svs;
    }

    public static boolean weaker(Node n1, Node n2) {

        return n1.isVariable() || n1.equals(n2);
    }
    
    public static HashMap<String, HashSet<String>> loadEndpointsFile (String file) {

        HashMap<String, HashSet<String>> endpoints = new HashMap<String, HashSet<String>>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String l = br.readLine();
            while (l!=null) {
                StringTokenizer st = new StringTokenizer(l);
                if (st.hasMoreTokens()) {
                    String fragment = st.nextToken();
                    HashSet<String> es = new HashSet<String>();
                    while (st.hasMoreTokens()) {
                        String e = st.nextToken();
                        es.add(e);
                    }
                    endpoints.put(fragment, es);
                }
                l = br.readLine();
            }
            br.close();

        } catch (IOException e) {
            System.err.println("Problems reading file: "+file);
            System.exit(1);
        }
        return endpoints;
    }
}
