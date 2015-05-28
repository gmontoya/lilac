import com.hp.hpl.jena.sparql.algebra.*;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.syntax.*;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.BasicPattern;

import java.util.*;

class myTransformTriple extends TransformCopy {

    HashMap<Node,Node> change;

    public myTransformTriple(HashMap<Node,Node> c) {
        this.change = c;
    }

    public Op transform(OpBGP opBGP) {
        //System.out.println("transform BGP "+opBGP);
        BasicPattern bp = opBGP.getPattern();
        Iterator<Triple> it = bp.iterator();
        BasicPattern r = new BasicPattern();
        while (it.hasNext()) {
            Triple t = it.next();
            if (change.containsKey(t.getSubject())) {
                t = new Triple(change.get(t.getSubject()), t.getPredicate(), t.getObject());
            }
            if (change.containsKey(t.getPredicate())) {
                t = new Triple(t.getSubject(), change.get(t.getPredicate()), t.getObject());
            }
            if (change.containsKey(t.getObject())) {
                t = new Triple(t.getSubject(), t.getPredicate(), change.get(t.getObject()));
            }
            r.add(t);
        }
        return new OpBGP(r);
    }

    public Op transform(OpTriple opTriple) {
        //System.out.println("transforming.."+opTriple.getTriple());
        Triple t = opTriple.getTriple();
            if (change.containsKey(t.getSubject())) {
                t = new Triple(change.get(t.getSubject()), t.getPredicate(), t.getObject());
            }
            if (change.containsKey(t.getPredicate())) {
                t = new Triple(t.getSubject(), change.get(t.getPredicate()), t.getObject());
            }
            if (change.containsKey(t.getObject())) {
                t = new Triple(t.getSubject(), t.getPredicate(), change.get(t.getObject()));
            }
        return new OpTriple(t);
    }
}
