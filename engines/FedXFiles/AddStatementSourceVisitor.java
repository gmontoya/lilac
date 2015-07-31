package com.fluidops.fedx.optimizer;

import java.util.*;

import org.openrdf.query.algebra.QueryModelNode;

import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.Service;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.algebra.Reduced;

import com.fluidops.fedx.structures.Endpoint;
import com.fluidops.fedx.algebra.ExclusiveStatement;
import com.fluidops.fedx.algebra.StatementSource;
import com.fluidops.fedx.algebra.StatementSource.StatementSourceType;
import com.fluidops.fedx.algebra.ExclusiveGroup;
import com.fluidops.fedx.algebra.SkipStatementPattern;
import com.fluidops.fedx.algebra.NJoin;
import com.fluidops.fedx.algebra.EmptyStatementPattern;
import com.fluidops.fedx.algebra.EmptyResult;
import com.fluidops.fedx.algebra.NUnion;
import com.fluidops.fedx.structures.QueryInfo;

import com.fluidops.fedx.exception.OptimizationException;

public class AddStatementSourceVisitor extends QueryModelVisitorBase<OptimizationException> {

    protected ArrayList<ArrayList<StatementPattern>> bgps = new ArrayList<ArrayList<StatementPattern>>();
    private HashMap<StatementPattern, HashSet<TreeSet<Endpoint>>> selectedSources;
    private HashMap<Endpoint, Set<StatementPattern>> options;
    private QueryInfo queryInfo;

    public AddStatementSourceVisitor(QueryInfo queryInfo, HashMap<StatementPattern, HashSet<TreeSet<Endpoint>>> selectedSources, HashMap<Endpoint, Set<StatementPattern>> options) {
        super();
        this.queryInfo = queryInfo;
        this.selectedSources = selectedSources;
        this.options = options;
    }

    public void addStatementSource() {
        //System.out.println("bgps: "+bgps);
        nextBGP:
            for (ArrayList<StatementPattern> bgp : bgps) {
                Iterator<StatementPattern> it = bgp.iterator();
                HashSet<StatementPattern> union = new HashSet<StatementPattern>();
                HashSet<StatementPattern> exclusive = new HashSet<StatementPattern>();
                HashSet<StatementPattern> covered = new HashSet<StatementPattern>();
                HashMap<Endpoint, HashSet<StatementPattern>> endpoints = new HashMap<Endpoint, HashSet<StatementPattern>>();
                HashSet<Endpoint> exclusiveSources = new HashSet<Endpoint>();
                // StatementPatterns are partitioned into 'union' and 'exclusive'
                while (it.hasNext()) {
                    StatementPattern t = it.next();
                    HashSet<TreeSet<Endpoint>> sources = selectedSources.get(t);
                    if (sources.size() > 1) {
                        union.add(t);
                        // for the 'union' StatementPattern, its endpoints options are loaded into 'endpoints'
                        Iterator<TreeSet<Endpoint>> it1 = sources.iterator();
                        while (it1.hasNext()) {
                            TreeSet<Endpoint> ss = it1.next();
                            Iterator<Endpoint> it2 = ss.iterator();
                            while (it2.hasNext()) {
                                Endpoint s = it2.next();
                                HashSet<StatementPattern> set2 = new HashSet<StatementPattern>(options.get(s));
                                set2.retainAll(bgp);
                                if (set2 == null) {
                                    set2 = new HashSet<StatementPattern>();
                                }
                                endpoints.put(s, set2);
                            }
                        }
                    } else {
                        exclusive.add(t);
                    }

                }
                List<TupleExpr> listOperators = new LinkedList<TupleExpr>();
                for (StatementPattern t : union) {
                    TupleExpr r = transform(t, endpoints, covered);
                    if (r instanceof EmptyResult) {
                        absorb(bgp, 0, bgp.size());
                        continue nextBGP;
                    }
                    listOperators.add(r);
                }
                endpoints.clear();
                HashSet<Endpoint> added = new HashSet<Endpoint>();
                for (StatementPattern e : exclusive) {
                    HashSet<TreeSet<Endpoint>> sourcesAux = selectedSources.get(e); 
                    TreeSet<Endpoint> sources = sourcesAux.iterator().next();
                    Iterator<Endpoint> it2 = sources.iterator();
                    while (it2.hasNext()) {
                        Endpoint endpoint = it2.next();
                        HashSet<StatementPattern> ss = endpoints.get(endpoint);
                        if (ss == null) {
                            ss = new HashSet<StatementPattern>();
                        }
                        ss.add(e);
                        endpoints.put(endpoint, ss);
                    }
                }
                for (Endpoint endpoint : endpoints.keySet()) {
                    HashSet<StatementPattern> triples0 = endpoints.get(endpoint);
                    ArrayList<HashSet<StatementPattern>> connectedTriples = getConnectedTriples(triples0);
                    for (HashSet<StatementPattern> triples : connectedTriples) {
                        if (covered.containsAll(triples)) {
                            continue;
                        }
                        StatementSource n = new StatementSource(endpoint.getId(), StatementSourceType.REMOTE);
                        List<ExclusiveStatement> list = new ArrayList<ExclusiveStatement>();
                        for (StatementPattern t : triples) {
                            list.add(new ExclusiveStatement(t, n, queryInfo));
                        }
                        TupleExpr aux = null;
                        if (list.size()>1) {
                            aux = new ExclusiveGroup(list, n, queryInfo);
                        } else if (list.size()==1) {
                            aux = list.get(0);
                        } else {
                            continue;
                        }
                        listOperators.add(aux);
                    }
                }
                remove(bgp, 1, bgp.size());
                StatementPattern toReplace = bgp.get(0);

                if (listOperators.size()>1) {
                    NJoin tmp = new NJoin(listOperators, queryInfo);
                    tmp.setParentNode(toReplace.getParentNode());
                    toReplace.replaceWith(tmp);
                } else {
                    toReplace.replaceWith(listOperators.get(0));
                }
            }
    }

    public TupleExpr transform(StatementPattern sp, HashMap<Endpoint, HashSet<StatementPattern>> endpoints, HashSet<StatementPattern> covered) {

        HashSet<TreeSet<Endpoint>> sources = selectedSources.get(sp);
        if (sources != null) {
            List<TupleExpr> args = new LinkedList<TupleExpr>();

            HashSet<StatementPattern> ts = null;
            HashSet<Endpoint> alreadyIncludedSources = new HashSet<Endpoint>();


            for (TreeSet<Endpoint> ss : sources) {
                
                Endpoint s = null;
                int max = 0;
                // From the alternative sources for a fragment, take the one that will incur in more joins
                for (Endpoint sAux : ss) {
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

                StatementSource n = new StatementSource(s.getId(), StatementSourceType.REMOTE);

                if (ts == null) {
                    ts = new HashSet<StatementPattern>(endpoints.get(s));
                } else {
                    ts.retainAll(endpoints.get(s));
                }

                List<ExclusiveStatement> tl = new ArrayList<ExclusiveStatement>();
                for (StatementPattern t : endpoints.get(s)) {
                    tl.add(new ExclusiveStatement(t, n, queryInfo));
                }
                tl.add(new ExclusiveStatement(sp, n, queryInfo));

                TupleExpr aux = null;
                if (tl.size()>1) {
                    aux = new ExclusiveGroup(tl, n, queryInfo);
                } else if (tl.size()==1) {
                    aux = tl.get(0);
                } else {
                    continue;
                }
                
                args.add(aux);
            }
            if (ts != null) {
                covered.addAll(ts);
            }
            if (args.size()>1) {
                return new NUnion(args, queryInfo);
            } else if (args.size() == 1) {
                return args.get(0);
            }
        }
        return new EmptyStatementPattern(sp);
    }

    public static ArrayList<HashSet<StatementPattern>> getConnectedTriples(HashSet<StatementPattern> triples) {

        ArrayList<HashSet<StatementPattern>> connected = new ArrayList<HashSet<StatementPattern>>();
        for (StatementPattern t : triples) {
            HashSet<StatementPattern> c = FedraQueryRewriter.getConnected(t, triples);
            ArrayList<HashSet<StatementPattern>> toRemove = new ArrayList<HashSet<StatementPattern>>();
            boolean add = true;
            for (HashSet<StatementPattern> d : connected) {
                if (c.containsAll(d)) {
                    toRemove.add(d);
                } else if (d.containsAll(c)) {
                    add = false;
                    break;
                }
            }
            for (HashSet<StatementPattern> d : toRemove) {
                connected.remove(d);
            }
            if (add && !connected.contains(c)) {
                connected.add(c);
            }
        }
        return connected;
    }

    private void remove(ArrayList<StatementPattern> bgp, int begin, int end) {
        for (int i = begin; i < end; i++) {
            StatementPattern toRemove = bgp.get(i);
            toRemove.replaceWith(new SkipStatementPattern());
        }
    }

    private void absorb(ArrayList<StatementPattern> bgp, int begin, int end) {

        for (int i = begin; i < end; i++) {
            StatementPattern toAbsorb = bgp.get(i);
            toAbsorb.replaceWith(new EmptyStatementPattern(toAbsorb));
        }
    }

    @Override
    public void meet(Union union) {
        super.meet(union);
    }

    @Override
    public void meet(LeftJoin leftjoin) {
        super.meet(leftjoin);
    }

    @Override
    public void meet(Filter filter) {
        super.meet(filter);
    }

    @Override
    public void meet(Service service) {
        super.meet(service);
    }

    @Override
    public void meet(Reduced reduced) {
        super.meet(reduced);
    }

    @Override
    public void meet(Join node) {
        ArrayList<StatementPattern> bgp = new ArrayList<StatementPattern>();
        ArrayList<TupleExpr> rest = extractBGP(node, bgp);
        if (!bgp.isEmpty()) {
            bgps.add(bgp);
        }
        for (TupleExpr te : rest) {
            te.visit(this);
        }
    }

        @Override
        public void meetOther(QueryModelNode node) {
                if (node instanceof NJoin) {
                        super.meetOther(node);          // depth first
                        meetNJoin((NJoin) node);
                } else {
                        super.meetOther(node);
                }
        }

        public void meetNJoin(NJoin node) {
                //System.out.println("tracing the BGPVisitor, inside meet NJoin");
                ArrayList<StatementPattern> bgp = new ArrayList<StatementPattern>();
                ArrayList<TupleExpr> rest = extractBGP(node, bgp);
                if (!bgp.isEmpty()) {
                    bgps.add(bgp);
                }
                for (TupleExpr te : rest) {
                    te.visit(this);
                }
        }

        private static ArrayList<TupleExpr> extractBGP(NJoin join, ArrayList<StatementPattern> bgp) {
            ArrayList<TupleExpr> rest = new ArrayList<TupleExpr>();
            for (int i = 0; i < join.getNumberOfArguments(); i++) {
                TupleExpr arg = join.getArg(i);
                if (arg instanceof StatementPattern) {
                    bgp.add((StatementPattern) arg);
                } else if (arg instanceof NJoin) {
                    extractBGP((NJoin) arg, bgp);
                } else {
                    rest.add(arg);
                }
            }
            return rest;
        }

    private static ArrayList<TupleExpr> extractBGP(Join join, ArrayList<StatementPattern> bgp) {
        ArrayList<TupleExpr> rest = new ArrayList<TupleExpr>();
        TupleExpr left = join.getLeftArg();
        TupleExpr right = join.getRightArg();
        if (left instanceof StatementPattern) {
            bgp.add((StatementPattern) left);
        } else if (left instanceof Join) {
            extractBGP((Join) left, bgp);
        } else {
            rest.add(left);
        }
        if (right instanceof StatementPattern) {
            bgp.add((StatementPattern) right);
        } else if (right instanceof Join) {
            extractBGP((Join) right, bgp);
        } else {
            rest.add(right);
        }
        return rest;
    }

    @Override
    public void meet(StatementPattern node) {
        //stmts.add(node);
    }
}
