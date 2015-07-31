package com.fluidops.fedx.optimizer;
import java.util.Comparator;
import com.fluidops.fedx.algebra.StatementSource;

public class StatementSourceComparator implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        StatementSource ss1 = (StatementSource) o1;
        StatementSource ss2 = (StatementSource) o2;

        int c = ss1.getEndpointID().compareTo(ss2.getEndpointID());

        return c;
    }
}
