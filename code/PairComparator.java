import java.util.Comparator;
import com.hp.hpl.jena.graph.Triple;

public class PairComparator<U,V> implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        Pair<U,V> p1 = (Pair<U,V>) o1;
        Pair<U,V> p2 = (Pair<U,V>) o2;
        
        int c = compareInt(p1.getFirst(),p2.getFirst());
        if (c==0) {
            c = compareInt(p1.getSecond(),p2.getSecond());
        }

        return c;
    }

    public static <T> int compareInt(T t1, T t2) {
        if (t1 instanceof Comparable && t2 instanceof Comparable) {
            Comparable c1 = (Comparable) t1;
            Comparable c2 = (Comparable) t2;
            return c1.compareTo(c2);
        } else if (t1 instanceof Triple && t2 instanceof Triple) {
            Triple tp1 = (Triple) t1;
            Triple tp2 = (Triple) t2;
            return (new TriplePatternComparator()).compare(tp1, tp2);
        } else {
            throw new UnsupportedOperationException();
        }
    }

}
