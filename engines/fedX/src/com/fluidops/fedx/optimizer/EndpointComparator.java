package com.fluidops.fedx.optimizer;
import java.util.Comparator;
import com.fluidops.fedx.structures.Endpoint;

public class EndpointComparator implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        Endpoint e1 = (Endpoint) o1;
        Endpoint e2 = (Endpoint) o2;
        int c = e1.getId().compareTo(e2.getId());
        return c;
    }
}
