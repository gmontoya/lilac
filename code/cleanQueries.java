import java.io.*;
import java.util.*;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Query;

/* Queries with triple patterns with the same node as subject and object causes FedX3.0 execution to abort.
   This class read a file with query per line and print in standard output the queries that do not have
   as subject and object the same node */

class cleanQueries {

    public static void main (String[] args) {

        String file = args[0];
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String l = br.readLine();
            while (l!=null) {
                Query q = QueryFactory.create(l); 
                ConjunctiveQuery cq = new ConjunctiveQuery(q,"q");
                List<Triple> triples = cq.getBody();
                boolean add = true;
                for (Triple t : triples) {
                    Node s = t.getSubject();
                    Node o = t.getObject();
                    if (s.equals(o)) {
                        add = false;
                        break;
                    }
                }
                if (add) {
                    q.setDistinct(true);
                    l = q.toString();
                    l = l.replace('\n', ' '); 
                    System.out.println(l);
                }
                l = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            System.err.println("Problems reading file: "+file);
            System.exit(1);
        }
    }
}
