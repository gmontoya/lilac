import java.util.*;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

public class TriplePatternFragment {

    protected Triple triple;
    private String dataset;
    private List<String> endpoints;

    public TriplePatternFragment(Triple t, String ds) {
        this.triple = t;
        this.dataset = ds;
        this.endpoints = new ArrayList<String>();
    }

    public void addSource(String e) {
        this.endpoints.add(e);
    }

    public String getDataset() {
        return this.dataset;
    }

    public int hashCode() {

        ArrayList<String> l = new ArrayList<String>();
        l.add(dataset);
        if (!triple.getPredicate().isVariable()) {
            l.add(triple.getPredicate().toString());
        } else {
            l.add(null);
        }
        if (!triple.getSubject().isVariable()) {
            l.add(triple.getSubject().toString());
        } else {
            l.add(null);
        }
        if (!triple.getObject().isVariable()) {
            l.add(triple.getObject().toString());
        } else {
            l.add(null);
        }
        return l.hashCode();
    }

    public boolean equals(Object o) {

        boolean e = o != null && (o instanceof TriplePatternFragment);
        if (e) {
            TriplePatternFragment other = (TriplePatternFragment) o;
            e = this.dataset.equals(other.dataset);
            e = e && exactMatch(this.triple.getPredicate(), other.triple.getPredicate());
            e = e && exactMatch(this.triple.getSubject(), other.triple.getSubject());
            e = e && exactMatch(this.triple.getObject(), other.triple.getObject());
        }
        return e;
    }

    public String toString() {
        return "<"+triple.toString()+", "+dataset+", "+endpoints.toString()+">";
    }

    public String getOneSource(boolean random) {
        if (random) {
            int i = (int) (Math.random()*endpoints.size());
            return endpoints.get(i);
        }
        return endpoints.get(0);
    }

    public boolean contains(TriplePatternFragment other) {

        boolean c = this.dataset.equals(other.dataset);
        c = c && weaker(this.triple.getPredicate(), other.triple.getPredicate());
        c = c && weaker(this.triple.getSubject(), other.triple.getSubject());
        c = c && weaker(this.triple.getObject(), other.triple.getObject());
        return c;
    }

    public boolean containedBy(TriplePatternFragment other) {

        boolean c = this.dataset.equals(other.dataset);
        c = c && weaker(other.triple.getPredicate(), this.triple.getPredicate());
        c = c && weaker(other.triple.getSubject(), this.triple.getSubject());
        c = c && weaker(other.triple.getObject(), this.triple.getObject());
        return c;
    }

    public boolean canAnswer(Triple t) {

        boolean c = true;
        c = c && compatible(this.triple.getPredicate(), t.getPredicate());
        c = c && compatible(this.triple.getSubject(), t.getSubject());
        c = c && compatible(this.triple.getObject(), t.getObject());
        return c;
    }

    public boolean contains(Triple t) {

        boolean c = true;
        c = c && weaker(this.triple.getPredicate(), t.getPredicate());
        c = c && weaker(this.triple.getSubject(), t.getSubject());
        c = c && weaker(this.triple.getObject(), t.getObject());
        return c;
    }

    public boolean exactMatch(Triple t) {

        boolean m = true;
        m = m && exactMatch(this.triple.getPredicate(), t.getPredicate());
        m = m && exactMatch(this.triple.getSubject(), t.getSubject());
        m = m && exactMatch(this.triple.getObject(), t.getObject());
        return m;
    }

    public static boolean weaker(Node v1, Node v2) {
        boolean b = (v1.isVariable() || (!v2.isVariable() && v1.equals(v2)));
        //System.out.println(b+" compatible "+vv+" and "+vq);
        return b;
    }

    public static boolean compatible(Node v1, Node v2) {
        boolean b = !(!v1.isVariable() && !v2.isVariable()) || v1.equals(v2);
        //System.out.println(b+" compatible "+vv+" and "+vq);
        return b;
    }

    public static boolean exactMatch(Node v1, Node v2) {
        boolean b = (v1.isVariable() && v2.isVariable()) || (!v1.isVariable() && !v2.isVariable() && v1.equals(v2));
        //System.out.println(b+" compatible "+vv+" and "+vq);
        return b;
    }

    protected List<String> getAllSources() {
        return this.endpoints;
    }
}
