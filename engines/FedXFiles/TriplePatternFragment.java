package com.fluidops.fedx.optimizer;

import java.util.*;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Var;

public class TriplePatternFragment {

    protected StatementPattern triple;
    private String dataset;
    private List<String> endpoints;

    public TriplePatternFragment(StatementPattern sp, String ds) {
        this.triple = sp;
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
        if (triple.getPredicateVar().hasValue()) {
            l.add(triple.getPredicateVar().getValue().stringValue());
        } else {
            l.add(null);
        }
        if (triple.getSubjectVar().hasValue()) {
            l.add(triple.getSubjectVar().getValue().stringValue());
        } else {
            l.add(null);
        }
        if (triple.getObjectVar().hasValue()) {
            l.add(triple.getObjectVar().getValue().stringValue());
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
            e = e && exactMatch(this.triple.getPredicateVar(), other.triple.getPredicateVar());
            e = e && exactMatch(this.triple.getSubjectVar(), other.triple.getSubjectVar());
            e = e && exactMatch(this.triple.getObjectVar(), other.triple.getObjectVar());
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
        c = c && weaker(this.triple.getPredicateVar(), other.triple.getPredicateVar());
        c = c && weaker(this.triple.getSubjectVar(), other.triple.getSubjectVar());
        c = c && weaker(this.triple.getObjectVar(), other.triple.getObjectVar());
        return c;
    }

    public boolean containedBy(TriplePatternFragment other) {

        boolean c = this.dataset.equals(other.dataset);
        c = c && weaker(other.triple.getPredicateVar(), this.triple.getPredicateVar());
        c = c && weaker(other.triple.getSubjectVar(), this.triple.getSubjectVar());
        c = c && weaker(other.triple.getObjectVar(), this.triple.getObjectVar());
        return c;
    }

    public boolean canAnswer(StatementPattern sp) {

        boolean c = true;
        c = c && compatible(this.triple.getPredicateVar(), sp.getPredicateVar());
        c = c && compatible(this.triple.getSubjectVar(), sp.getSubjectVar());
        c = c && compatible(this.triple.getObjectVar(), sp.getObjectVar());
        return c;
    }

    public boolean contains(StatementPattern sp) {

        boolean c = true;
        c = c && weaker(this.triple.getPredicateVar(), sp.getPredicateVar());
        c = c && weaker(this.triple.getSubjectVar(), sp.getSubjectVar());
        c = c && weaker(this.triple.getObjectVar(), sp.getObjectVar());
        return c;
    }

    public boolean exactMatch(StatementPattern sp) {

        boolean m = true;
        m = m && exactMatch(this.triple.getPredicateVar(), sp.getPredicateVar());
        m = m && exactMatch(this.triple.getSubjectVar(), sp.getSubjectVar());
        m = m && exactMatch(this.triple.getObjectVar(), sp.getObjectVar());
        return m;
    }

    public static boolean weaker(Var v1, Var v2) {
        boolean b = (!v1.hasValue() || (v2.hasValue() && v1.getValue().equals(v2.getValue())));
        //System.out.println(b+" compatible "+vv+" and "+vq);
        return b;
    }

    public static boolean compatible(Var v1, Var v2) {
        boolean b = !(v1.hasValue() && v2.hasValue()) || v1.getValue().equals(v2.getValue());
        //System.out.println(b+" compatible "+vv+" and "+vq);
        return b;
    }

    public static boolean exactMatch(Var v1, Var v2) {
        boolean b = (!v1.hasValue() && !v2.hasValue()) || (v1.hasValue() && v2.hasValue() && v1.getValue().equals(v2.getValue()));
        //System.out.println(b+" compatible "+vv+" and "+vq);
        return b;
    }

    protected List<String> getAllSources() {
        return this.endpoints;
    }
}
