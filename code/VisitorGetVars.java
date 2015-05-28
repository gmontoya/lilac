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

public class VisitorGetVars extends OpVisitorBase {

    private HashSet<String> vars = new HashSet<String>();

    public VisitorGetVars() {
        super();
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
        Node n = t.getSubject();
        if (n.isVariable()) {
            this.vars.add(n.getName());
        }
        n = t.getPredicate();
        if (n.isVariable()) {
            this.vars.add(n.getName());
        }
        n = t.getObject();
        if (n.isVariable()) {
            this.vars.add(n.getName());
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

    public HashSet<String> getVars() {
        return this.vars;
    }

    public static void main (String[] args) {

        String queryIn = args[0];
        //String publicEndpointsFile = args[1];
        //HashSet<String> publicEndpoints = new HashSet<String>();
        //fedra.loadEndpoints(publicEndpointsFile, publicEndpoints);
        try {
            Query q = QueryFactory.read(queryIn);
            Op op = (new AlgebraGenerator()).compile(q);
            VisitorGetVars vcs = new VisitorGetVars();
            OpWalker.walk(op, vcs);
            System.out.print(vcs.getVars());
        } catch (com.hp.hpl.jena.query.QueryParseException e) {
            System.out.print(0);
        }
    }
}
