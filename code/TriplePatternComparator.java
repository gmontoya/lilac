import java.util.Comparator;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.Node;

public class TriplePatternComparator implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        Triple tp1 = (Triple) o1;
        Triple tp2 = (Triple) o2;
        NodeComparator nc = new NodeComparator();
        int c = nc.compare(tp1.getPredicate(),tp2.getPredicate());
        if (c==0) {
            c = nc.compare(tp1.getSubject(),tp2.getSubject());
        }
        if (c==0) {
            c = nc.compare(tp1.getObject(),tp2.getObject());
        }

        return c;
    }

    class NodeComparator implements Comparator {

        @Override
        public int compare(Object o1, Object o2) {
            Node n1 = (Node) o1;
            Node n2 = (Node) o2;
            int c = n1.toString().compareTo(n2.toString());
            return c;
        }
    }
}
