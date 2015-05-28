import com.hp.hpl.jena.sparql.algebra.*;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.syntax.*;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.core.Var;


import java.util.*;

class TransformInOneDotOneSPARQL2 extends TransformCopy {

    HashMap<Triple, HashSet<TreeSet<String>>> selectedSources;
    HashMap<Triple, Set<String>> options;

    ExprList el;

    public TransformInOneDotOneSPARQL2(HashMap<Triple, HashSet<TreeSet<String>>> ss, ExprList el, HashMap<Triple, Set<String>> os) {
        this.selectedSources = ss;
        this.options = os;
        this.el = el;
        //System.out.println("creating transformer with exprlist: "+el.toString());
    }

    public Op transform(OpNull opNull) {

        return opNull;
    }

    public Op transform(OpDistinct opDistinct, Op subOp) {

/*        Op newSubOp = subOp;
        if (!ready(newSubOp)) {
            newSubOp = Transformer.transform(this, subOp);
        }
        if (newSubOp instanceof OpNull) {
            return newSubOp;
        }
        return new OpDistinct(newSubOp);*/
        if (subOp instanceof OpNull) {
            return subOp;
        }
        return new OpDistinct(subOp);
    }

    public Op transform(OpService opService, Op subOp) {
        //System.out.println("transforming opService: "+opService);
        if (subOp instanceof OpNull) {
            return subOp;
        }   
        return new OpService(opService.getService(), subOp, opService.getSilent());
/*
        if (opService.equals(subOp)) { //((subOp instanceof OpService) && ((OpService) subOp).getService().equals(opService.getService())) {
            return subOp;
        }
        //System.out.println("subop de service: "+subOp.toString());
        //System.out.println("service: "+opService.toString()); 
        Op newSubOp = Transformer.transform(this, subOp);
        if (newSubOp instanceof OpNull) {
            return newSubOp;
        }
        return new OpService(opService.getService(), newSubOp, opService.getSilent());
*/
    }

    public Op transform(OpProject opProject, Op subOp) {

        if (subOp instanceof OpNull) {
            return subOp;
        }   
        return new OpProject(subOp, opProject.getVars());

/*        Op newSubOp = subOp;
        if (!ready(newSubOp)) {
            newSubOp = Transformer.transform(this, subOp);
        }
        if (newSubOp instanceof OpNull) {
            return newSubOp;
        }
        return new OpProject(newSubOp, opProject.getVars());*/
    }

    private boolean ready(Op op) {

        VisitorCountService vcs = new VisitorCountService();
        OpWalker ow = new OpWalker();
        ow.walk(op, vcs);
        int c1 = vcs.getCount();
        VisitorCountTriples vct = new VisitorCountTriples();                      
        ow.walk(op, vct);
        int c2 = vct.getCount();
        return (c1 == c2);
    }

    private String exclusive(Op op, String e, boolean b) {

        VisitorExclusiveGroup veg = new VisitorExclusiveGroup(selectedSources, e, b);
        OpWalker ow = new OpWalker();
        ow.walk(op, veg);
        boolean b1 = veg.isExclusive();
        if (b1) {
            return veg.getEndpoint();
        } else {
            return null;
        }
    }

    private String exclusive(Op op1, Op op2, String e, boolean b) {

        VisitorExclusiveGroup veg = new VisitorExclusiveGroup(selectedSources, e, b);
        OpWalker ow = new OpWalker();
        ow.walk(op1, veg);
        boolean b1 = veg.isExclusive();
        if (b1) {
            String e1 = veg.getEndpoint();
            veg = new VisitorExclusiveGroup(selectedSources, e1, b1);
            OpWalker.walk(op2, veg);
            boolean b2 = veg.isExclusive();
            String e2 = veg.getEndpoint();
            if (b2) {
                return e2;
            }
        }
        return null;
    }

    private Op clean(Op op) {

        Transform t = new TransformDeleteServices();
        Op newOp = Transformer.transform(t, op);
        return newOp;
    }

    public Op transform(OpUnion opUnion, Op left, Op right) {
        //System.out.println("transforming union: "+opUnion);
        String e = exclusive(left, right, null, true);        
        //System.out.println("exclusive: "+e);
        if (e != null) {
            Node n = NodeFactory.createURI(e);
            Op op = left;
            //System.out.println("left: "+left);
            //System.out.println("right: "+right+". class: "+right.getClass());
            if (left instanceof OpNull) {
                op = right;
            } else if (!(right instanceof OpNull)) {
                op = OpUnion.create(left, right);
            }
            op = new OpService(n, clean(op), false);
            //System.out.println("op in l119: "+op);
            return op;
        }
        if (left instanceof OpNull) {
            return right;
        } else if (right instanceof OpNull) {
            return left; 
        } else {
            return OpUnion.create(left, right);
        }
/*
        Op newLeft = left;
        if (!ready(newLeft)) { 
            newLeft = Transformer.transform(this, left);
        }
        Op newRight = right;
        if (!ready(newRight)) {
            newRight = Transformer.transform(this, right);
        }
        if (newLeft instanceof OpNull) {
            return newRight;
        } else if (newRight instanceof OpNull) {
            return newLeft;
        } else {
            return OpUnion.create(newLeft, newRight);
        }*/
        //return opUnion.apply(this, left, right);
    }

    public Op transform(OpLeftJoin opLeftJoin, Op left, Op right) {
        //System.out.println("transforming leftjoin: "+opLeftJoin);
        String e = exclusive(left, right, null, true);
        //System.out.println("exclusive: "+e);
        if (e != null) {
            Node n = NodeFactory.createURI(e);
            Op op = left;
            if (left instanceof OpNull) {
                op = left;
            } else if (right instanceof OpNull) {
                op = left;
            } else {
                op = OpLeftJoin.create(left, right, opLeftJoin.getExprs());
            }
            op = new OpService(n, clean(op), false);
            return op;
        }
        /*System.out.println("transforming the subops: "+left+" and "+right);
        Op newLeft = left;
        if (!ready(newLeft)) {
            newLeft = Transformer.transform(this, left);
        }
        Op newRight = right;
        if (!ready(newRight)) {
            newRight = Transformer.transform(this, right);
        }*/
        if (left instanceof OpNull) {
            return left;
        } else if (right instanceof OpNull) {
            return left;
        } else {
            return OpLeftJoin.create(left, right, opLeftJoin.getExprs());
        }
        //return opLeftJoin.apply(this, left, right);
    }

    public Op transform(OpJoin opJoin, Op left, Op right) {
        //System.out.println("tranforming join: "+opJoin);
        String e = exclusive(left, right, null, true);
        //System.out.println("exclusive: "+e);
        if (e != null) {
            Node n = NodeFactory.createURI(e);
            Op op = left;
            if (left instanceof OpNull) {
                op = left;
            } else if (right instanceof OpNull) {
                op = right;
            } else { 
                op = OpJoin.create(left, right);
            }
            op = new OpService(n, clean(op), false);
            return op;
        }
/*
        Op newLeft = left;
        if (!ready(newLeft)) {
            newLeft = Transformer.transform(this, left);
        }
        Op newRight = right;
        if (!ready(newRight)) {
            newRight = Transformer.transform(this, right);
        }*/
        if (left instanceof OpNull) {
            return left;
        } else if (right instanceof OpNull) {
            return right;
        } else {
            return OpJoin.create(left, right);
        }
        //return opJoin.apply(this, left, right);
    }

    public Op transform(OpGroup opGroup, Op subOp) {
        //System.out.println("tranforming group: "+opGroup);
        String e = exclusive(opGroup, subOp, null, true); 
        //System.out.println("exclusive: "+e);
        if (e != null) {
            Node n = NodeFactory.createURI(e);
            Op op = subOp;
            if (!(subOp instanceof OpNull)) {
                op = new OpGroup(subOp, opGroup.getGroupVars(), opGroup.getAggregators());
            }
            op = new OpService(n, clean(op), false);
            return op;
        }/*
        Op newSubOp = subOp;
        if (!ready(newSubOp)) {                                                                                                  
            newSubOp = Transformer.transform(this, subOp);
        }*/
        if (subOp instanceof OpNull) {
            return subOp;
        }
        return new OpGroup(subOp, opGroup.getGroupVars(), opGroup.getAggregators());
        //return opGroup.apply(this, subOp);

    }

    public Op transform(OpFilter opFilter, Op subOp) {
        //System.out.println("tranforming filter: "+opFilter);
        String e = exclusive(opFilter, subOp, null, true); 
        //System.out.println("exclusive: "+e);
        if (e != null) {
            Node n = NodeFactory.createURI(e);
            Op op = subOp;
            if (!(subOp instanceof OpNull)) {
                op = OpFilter.filter(opFilter.getExprs(), subOp);
            }
            op = new OpService(n, clean(op), false);
            return op;
        }/*
        Op newSubOp = subOp;
        if (!ready(newSubOp)) {
            newSubOp = Transformer.transform(this, subOp);                                                                       
        }*/
        if (subOp instanceof OpNull) {
            return subOp;
        }
        return OpFilter.filter(opFilter.getExprs(), subOp);
    }

    public Op transform(OpBGP opBGP) {
        //System.out.println("transform BGP "+opBGP);
        BasicPattern bp = opBGP.getPattern();
        Iterator<Triple> it = bp.iterator();
        Op u = OpNull.create();
        HashSet<Triple> union = new HashSet<Triple>();
        HashSet<Triple> exclusive = new HashSet<Triple>();
        HashSet<Triple> covered = new HashSet<Triple>();
        HashMap<String, HashSet<Triple>> endpoints = new HashMap<String, HashSet<Triple>>();
        HashSet<String> exclusiveSources = new HashSet<String>();
        while (it.hasNext()) {
            Triple t = it.next();
            HashSet<TreeSet<String>> sources = selectedSources.get(t);
            if (sources.size() > 1) {
                union.add(t);
                Iterator<TreeSet<String>> it1 = sources.iterator();
                while (it1.hasNext()) {
                    TreeSet<String> ss = it1.next();
                    Iterator<String> it2 = ss.iterator();
                    while (it2.hasNext()) {
                        String s = it2.next();
                        HashSet set2 = new HashSet<Triple>();
                        endpoints.put(s, set2);
                    }
                }
            } else {
                exclusive.add(t);
                ////exclusiveSources.add(sources.iterator().next());
            }

        }
        // set: triples to evaluate in more than one endpoint
        // endpoints: map: Endpoint --> set of Triple, for triple with just one source
        //System.out.println("endpoints: "+endpoints);
        //System.out.println("exclusive: "+exclusive);
        //System.out.println("union: "+union);

        if (options != null) {
            HashSet<String> toDelete = new HashSet<String>();
            it = bp.iterator();
            // increase the query shipping
            while (it.hasNext()) {
                Triple t = it.next();
                Set<String> ops = options.get(t);
                if (ops == null) {
                    continue;
                }
                for (String o : ops) {
                    HashSet<Triple> ts = endpoints.get(o);
                    if (ts != null) {
                        ts.add(t);
                        endpoints.put(o, ts);
                    }
                }
            }/*
            HashSet<String> toRemove = new HashSet<String>();
            for (String es : exclusiveSources) {
                for (String es2 : exclusiveSources) {
                    if (!es.equals(es2) && endpoints.get(es).containsAll(endpoints.get(es2))) {
                        toRemove.add(es2);
                    }
                }
            }
            for (String s : toRemove) {
                exclusiveSources.remove(s);
            }*/
            //System.out.println("endpoints: "+endpoints);
        }

        //System.out.println("endpoints: "+endpoints);
        List<Op> listOperators = new ArrayList<Op>();        
        for (Triple t : union) {
            OpTriple opTriple = new OpTriple(t);
            Op r = transform(opTriple, endpoints, covered);
            if (r instanceof OpNull) {
                return r;
            }
            /*if (u instanceof OpNull) {
                u = r;
            } else {
                u = OpJoin.create(u, r);
            }*/
            listOperators.add(r);
        }

        //exclusive.removeAll(covered);
        endpoints.clear();
        HashSet<String> added = new HashSet<String>();
        for (Triple e : exclusive) {
             HashSet<TreeSet<String>> sourcesAux = selectedSources.get(e); 
             TreeSet<String> sources = sourcesAux.iterator().next();
             Iterator<String> it2 = sources.iterator();
             while (it2.hasNext()) {
                 String endpoint = it2.next();
                 HashSet<Triple> ss = endpoints.get(endpoint);
                 if (ss == null) {
                     ss = new HashSet<Triple>();
                 }
                 ss.add(e);
                 endpoints.put(endpoint, ss);
             }
        }
        //List<Op> listOperators = new ArrayList<Op>();
        for (String endpoint : endpoints.keySet()) {
            HashSet<Triple> triples0 = endpoints.get(endpoint);
            ArrayList<HashSet<Triple>> connectedTriples = getConnectedTriples(triples0);
            for (HashSet<Triple> triples : connectedTriples) {
                if (covered.containsAll(triples)) {
                    continue;
                }
                Node n = NodeFactory.createURI(endpoint);
                ArrayList<Triple> tl = new ArrayList<Triple>(triples);
                List<String> lv = new ArrayList<String>();
                for (Triple t : tl) {
                    lv.addAll(getVars(t));
                }
                ExprList l0 = getExprConcerned(lv, el);
                BasicPattern bp2 = BasicPattern.wrap(tl);
                Op aux = clean(new OpBGP(bp2));
                if (l0.size()>0) {
                    aux = OpFilter.filter(l0, aux);
                }
                aux = new OpService(n, aux, false);
            /*  if (u instanceof OpNull) {
                    u = aux;
                } else {
                    u = OpJoin.create(u, aux); 
                }*/
                listOperators.add(aux);
            }
        }
        OpSequence ops = OpSequence.create();
        List<Op> listOperatorsSorted = fixJoinOrder(listOperators);
        //u = ops.copy(listOperatorsSorted);
        u = makePlan(listOperatorsSorted);
        return u;
    }

    public static Op makePlan(List<Op> listOps) {

        Op result = listOps.get(0);
        for (int i = 1; i < listOps.size(); i++) {
            result = OpJoin.create(result, listOps.get(i));
        }
        return result;
    }

    public static List<Op> fixJoinOrder(List<Op> listOperators) {
        List<Op> result = new ArrayList<Op>();
        List<Op> left = listOperators;
        int n = listOperators.size();
        HashSet<String> joinvars = new HashSet<String>();
        for (int i = 0; i < n; i++) {
            double mincost = 2;
            Op arg = null;
            for (Op j : left) {
                double cost = estimateCost(j, joinvars);
                if (cost < mincost) {
                    arg = j;
                    mincost = cost;
                }
            }
            joinvars.addAll(vars(arg));
            result.add(arg);
            left.remove(arg);
        }
        return result;
    }

    public static double estimateCost(Op j, HashSet<String> joinvars) {
        VisitorEstimateCost vec = new VisitorEstimateCost(joinvars);
        OpWalker.walk(j, vec);
        double c = vec.getCost();
        return c;
    }

    public static HashSet<String> vars (Op op) {
        VisitorGetVars vgvs = new VisitorGetVars();
        OpWalker.walk(op, vgvs);
        return vgvs.getVars();
    }

    public static ArrayList<HashSet<Triple>> getConnectedTriples(HashSet<Triple> triples) {

        ArrayList<HashSet<Triple>> connected = new ArrayList<HashSet<Triple>>();
        for (Triple t : triples) {
            HashSet<Triple> c = fedra2.getConnected(t, triples);
            ArrayList<HashSet<Triple>> toRemove = new ArrayList<HashSet<Triple>>();
            boolean add = true;
            for (HashSet<Triple> d : connected) {
                if (c.containsAll(d)) {
                    toRemove.add(d);
                } else if (d.containsAll(c)) {
                    add = false;
                    break;
                }
            }
            for (HashSet<Triple> d : toRemove) {
                connected.remove(d);
            }
            if (add && !connected.contains(c)) {
                connected.add(c);
            }
        }
        return connected;
    }

    public static List<String> getVars(Triple t) {

        List<String> l = new ArrayList<String>();
        Node n = t.getObject();
        if (n.isVariable()) {
            l.add(n.getName());
        }
        n = t.getPredicate();                                                                                                  
        if (n.isVariable()) {                                                                                                    
            l.add(n.getName());                                                                                                  
        }
        n = t.getSubject();                                                                                                  
        if (n.isVariable()) {                                                                                                    
            l.add(n.getName());                                                                                                  
        }
        return l;
    }

    public static ExprList getExprConcerned(List<String> l, ExprList el) {

        //System.out.println("searching concerned for triple elements: "+l);
        ExprList r = new ExprList();
        Iterator<Expr> i = el.iterator();
        while (i.hasNext()) {
            Expr e = i.next();
            Collection<Var> vs = new ArrayList<Var>();
            e.varsMentioned(vs);
            //System.out.println("mentioned variables in "+e.toString()+" are: "+Arrays.toString(vs.toArray()));
            for (Var v : vs) {
                if (l.contains(v.getVarName())) {
                    r.add(e);
                    break;
                }
            }
        }
        return r;
    }

    public static HashSet<String> joinVars(Triple t1, Triple t2) {

        HashSet<String> varsT1 = new HashSet<String>();
        HashSet<String> varsT2 = new HashSet<String>();
        if (t1.getSubject().isVariable()) {
            varsT1.add(t1.getSubject().getName());
        }
        if (t1.getPredicate().isVariable()) {                                                  
            varsT1.add(t1.getPredicate().getName());                                                    
        }
        if (t1.getObject().isVariable()) {                                                                                                                                     
            varsT1.add(t1.getObject().getName());                                                                                                                               
        }
        if (t2.getSubject().isVariable()) {                                                                                                                                     
            varsT2.add(t2.getSubject().getName());                                                                                                                               
        }
        if (t2.getPredicate().isVariable()) {
            varsT2.add(t2.getPredicate().getName());
        }
        if (t2.getObject().isVariable()) { 
            varsT2.add(t2.getObject().getName()); 
        }
        varsT1.retainAll(varsT2);
        return varsT1;
    }

    public static ArrayList<Triple> prune(ArrayList<Triple> tl, Triple t) {

        HashSet<Triple> tk = new HashSet<Triple>();
        ArrayList<Triple> tm = new ArrayList<Triple>(tl);
        while (true) {
        int size = tk.size();
        for (Triple t2 : tm) {
            if (joinVars(t2, t).size()>0) {
                tk.add(t2);
            }
             ArrayList<Triple> toAdd = new  ArrayList<Triple>();
            for (Triple t3 : tk) {
                if (joinVars(t2, t3).size()>0) {                                                                                                                                     
                    toAdd.add(t2);
                }
            }
            for (Triple t3 : toAdd) {
                tk.add(t3);
            }
        }
        if (size == tk.size()) { break; }
        }
        tm = new ArrayList<Triple>();
        for (Triple t2 : tk) {
           tm.add(t2);
        }
        return tm;
    } 

    public Op transform(OpTriple opTriple, HashMap<String, HashSet<Triple>> endpoints, HashSet<Triple> covered) {
        //System.out.println("transformingTriple: "+opTriple.getTriple());
        Triple t = opTriple.getTriple();
        HashSet<TreeSet<String>> sources = selectedSources.get(t);
        if (sources != null) {
            Op r = OpNull.create();
            HashSet<Triple> ts = null;
            HashSet<String> alreadyIncludedSources = new HashSet<String>();
            for (TreeSet<String> ss : sources) {
                String s = null;
                int max = 0;
                for (String sAux : ss) {
                    int size = endpoints.get(sAux) != null ? endpoints.get(sAux).size() : 0;
                    if ((s == null) || (size > max)) {
                        max = size;
                        s = sAux;
                    }
                }
                if (alreadyIncludedSources.contains(s)) {
                    continue;
                }
                alreadyIncludedSources.add(s);
                Node n = NodeFactory.createURI(s);
                ArrayList<Triple> tl = new ArrayList<Triple>(endpoints.get(s));
                //prune(tl, t);
                if (ts == null) {
                    ts = new HashSet<Triple>(tl);
                } else {
                    ts.retainAll(tl);
                }
                tl.add(t);
                List<String> lv = new ArrayList<String>();
                for (Triple t2 : tl) {
                   lv.addAll(getVars(t2));
                }
                ExprList l0 = getExprConcerned(lv, el);
                BasicPattern bp2 = BasicPattern.wrap(tl);
                Op aux = clean(new OpBGP(bp2));
                if (l0.size()>0) {
                   aux = OpFilter.filter(l0, aux);
                }
                aux = new OpService(n, aux, false);

                if (r instanceof OpNull) {
                    r = aux;
                } else {
                    r = new OpUnion(r, aux);
                }
            }
            if (ts != null) {
                covered.addAll(ts);
            }
            return r;
        }
        return OpNull.create();
    }
}
