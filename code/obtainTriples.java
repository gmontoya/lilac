import com.hp.hpl.jena.graph.Triple;
import java.util.List;
import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.graph.Node;

class obtainTriples {

    public static void main(String[] args) {
        String file = args[0];
        ConjunctiveQuery cq = new ConjunctiveQuery(file);
        List<Triple> ts = cq.getBody();
        for (Triple t : ts) {
            Node n = t.getSubject();
            String s = getString(n);
            n = t.getPredicate();
            String p = getString(n);
            n = t.getObject();
            String o = getString(n);
            System.out.println(s+" "+p+" "+o);
            //System.out.println(PrintUtil.print(t));
        }
    }

    public static String getString(Node n) {
        String str = n.toString();
        if (n.isURI()) {
            str = "<"+str+">";
        }
        return str;
    }
}
