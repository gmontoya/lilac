package com.fluidops.fedx.optimizer;
import java.util.Comparator;
import org.openrdf.query.algebra.StatementPattern;
import com.fluidops.fedx.structures.Endpoint;
//import com.fluidops.fedx.EndpointComparator;
//import com.fluidops.fedx.StatementPatternComparator;

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
        } else if (t1 instanceof Endpoint && t2 instanceof Endpoint) {
            Endpoint e1 = (Endpoint) t1;
            Endpoint e2 = (Endpoint) t2;
            return (new EndpointComparator()).compare(e1, e2);
        } else if (t1 instanceof StatementPattern && t2 instanceof StatementPattern) {
            StatementPattern sp1 = (StatementPattern) t1;
            StatementPattern sp2 = (StatementPattern) t2;
            return (new StatementPatternComparator()).compare(sp1, sp2);
        } else {
            throw new UnsupportedOperationException();
        }
    }

}
