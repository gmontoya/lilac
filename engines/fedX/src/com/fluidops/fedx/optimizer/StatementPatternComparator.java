package com.fluidops.fedx.optimizer;
import java.util.Comparator;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.StatementPattern;

public class StatementPatternComparator implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        StatementPattern sp1 = (StatementPattern) o1;
        StatementPattern sp2 = (StatementPattern) o2;
        VarComparator vc = new VarComparator();
        int c = vc.compare(sp1.getPredicateVar(),sp2.getPredicateVar());
        if (c==0) {
            c = vc.compare(sp1.getSubjectVar(),sp2.getSubjectVar());
        }
        if (c==0) {
            c = vc.compare(sp1.getObjectVar(),sp2.getObjectVar());
        }

        return c;
    }

    class VarComparator implements Comparator {

        @Override
        public int compare(Object o1, Object o2) {
            Var v1 = (Var) o1;
            Var v2 = (Var) o2;
            int c = v1.getName().compareTo(v2.getName());
            if ((c==0) && (v1.hasValue() && v2.hasValue())) {
                c = v1.getValue().stringValue().compareTo(v2.getValue().stringValue());
            }
            return c;
        }
    }
}
