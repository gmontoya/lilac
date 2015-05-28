//package semLAV;

import com.hp.hpl.jena.sparql.algebra.OpVisitorBase;
import java.util.*;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.algebra.OpWalker;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.algebra.AlgebraGenerator;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.*;

public class VisitorExclusiveGroup extends OpVisitorBase {

    TreeSet<String> endpoint;
    boolean exclusive;
    HashMap<Triple, HashSet<TreeSet<String>>> selectedSources;

    public VisitorExclusiveGroup(HashMap<Triple, HashSet<TreeSet<String>>> ss) {
        super();
        exclusive = true;
        selectedSources = ss;
        endpoint = null;
    }

    public VisitorExclusiveGroup(HashMap<Triple, HashSet<TreeSet<String>>> ss, String e, boolean b) {
        super();
        //System.out.println("Starting visitor with e: "+e+" and b: "+b);
        exclusive = b;
        selectedSources = ss;
        endpoint = new TreeSet<String>();
        endpoint.add(e);
    }

    public void visit(OpLeftJoin opLeftJoin) {
        //System.out.println("visitingLeftJoin "+opLeftJoin);
        Op left = opLeftJoin.getLeft();
        Op right = opLeftJoin.getRight();
        //System.out.println("before left, endpoint: "+endpoint+". exclusive: "+exclusive);
        left.visit(this);
        //System.out.println("before right, endpoint: "+endpoint+". exclusive: "+exclusive);
        right.visit(this);
        //System.out.println("after right, endpoint: "+endpoint+". exclusive: "+exclusive);
    }

    public void visit(OpJoin opJoin) {

        Op left = opJoin.getLeft();
        Op right = opJoin.getRight();
        left.visit(this);
        right.visit(this);
    }

    public void visit(OpUnion opUnion) {

        Op left = opUnion.getLeft();
        Op right = opUnion.getRight();
        left.visit(this);
        right.visit(this);
    }

    public void visit(OpTriple opTriple) {
        //System.out.println("visiting "+opTriple+" endpoint: "+endpoint+" exclusive: "+exclusive);
        HashSet<TreeSet<String>> hse = selectedSources.get(opTriple.getTriple());
        Iterator<TreeSet<String>> it = hse.iterator();
        TreeSet<String> eaux = null;
        if (it.hasNext()) {
            eaux = it.next();
        }
        if (endpoint != null) {
            eaux.retainAll(endpoint);
        }
        exclusive = exclusive && (hse.size() == 1) && (endpoint == null || eaux.size() > 0);
        endpoint = eaux;
    }

    public void visit (OpBGP opBGP) {
        //System.out.println("visitingBGP "+opBGP);
        BasicPattern bp = opBGP.getPattern();
        Iterator<Triple> it = bp.iterator();
        while (it.hasNext()) {
            Triple t = it.next();
            OpTriple opTriple = new OpTriple(t);
            visit(opTriple);
        }
    }

    public boolean isExclusive() {
        return exclusive;
    }

    public String getEndpoint() {
        if (endpoint != null && endpoint.size() > 0) {
            return endpoint.iterator().next();
        } else {
            return null;
        }
    }
/*
    public static void main (String[] args) {

        String queryIn = args[0];
        //String publicEndpointsFile = args[1];
        //HashSet<String> publicEndpoints = new HashSet<String>();
        //fedra.loadEndpoints(publicEndpointsFile, publicEndpoints);
        Query q = QueryFactory.read(queryIn);
        Op op = (new AlgebraGenerator()).compile(q);
        VisitorExclusiveGroup veg = new VisitorExclusiveGroup();
        OpWalker.walk(op, veg);
        boolean eg = veg.isExclusive();
        System.out.print(eg);
    }*/
}
