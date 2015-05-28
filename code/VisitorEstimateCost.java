//package semLAV;

import com.hp.hpl.jena.sparql.algebra.OpVisitorBase;
import java.util.*;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.algebra.OpWalker;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.algebra.AlgebraGenerator;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.graph.Node;

public class VisitorEstimateCost extends OpVisitorBase {

    private int nn = 0;
    private int nv = 0;
    private HashSet<String> joinVars = null;
    private double cost = 0.0;

    public VisitorEstimateCost(HashSet<String> jvs) {
        super();
        this.joinVars = jvs;
    }

/*
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
*/
    public void visit(OpTriple opTriple) {
        Triple t = opTriple.getTriple();
        nn = nn + 3;
        Node n = t.getSubject();
        if (n.isVariable() && !this.joinVars.contains(n.getName())) {
            nv++;
        }
        n = t.getPredicate();
        if (n.isVariable() && !this.joinVars.contains(n.getName())) {
            nv++;
        }
        n = t.getObject();
        if (n.isVariable() && !this.joinVars.contains(n.getName())) {
            nv++;
        }
    }

    public void visit (OpBGP opBGP) {

        BasicPattern bp = opBGP.getPattern();
        Iterator<Triple> it = bp.iterator();
        while (it.hasNext()) {
            Triple t = it.next();
            OpTriple opTriple = new OpTriple(t);
            visit(opTriple);
        }
    }

    public double getCost() {
        this.cost = ((double) this.nv)/this.nn;
        return this.cost;
    }

    public static void main (String[] args) {

        String queryIn = args[0];
        //String publicEndpointsFile = args[1];
        //HashSet<String> publicEndpoints = new HashSet<String>();
        //fedra.loadEndpoints(publicEndpointsFile, publicEndpoints);
        try {
            Query q = QueryFactory.read(queryIn);
            Op op = (new AlgebraGenerator()).compile(q);
            HashSet<String> hs = new HashSet<String>();
            hs.add("x1");
            VisitorEstimateCost vcs = new VisitorEstimateCost(hs);
            OpWalker.walk(op, vcs);
            double c = vcs.getCost();
            System.out.print(c);
        } catch (com.hp.hpl.jena.query.QueryParseException e) {
            System.out.print(0);
        }
    }
}
