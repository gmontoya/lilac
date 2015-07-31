package com.fluidops.fedx.optimizer;

import java.util.*;

import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.Service;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.algebra.Reduced;
import org.openrdf.query.algebra.QueryModelNode;
import com.fluidops.fedx.algebra.NJoin;
import com.fluidops.fedx.exception.OptimizationException;

public class BGPVisitor extends QueryModelVisitorBase<OptimizationException> {

	protected ArrayList<ArrayList<StatementPattern>> bgps = new ArrayList<ArrayList<StatementPattern>>();
		
	public BGPVisitor() {
		super();
	}

	public ArrayList<ArrayList<StatementPattern>> getBGPs() {
		return bgps;
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
                ArrayList<StatementPattern> bgp = new ArrayList<StatementPattern>();
                ArrayList<TupleExpr> rest = extractBGP(node, bgp);
                if (!bgp.isEmpty()) {
                    bgps.add(bgp);
                }
                for (TupleExpr te : rest) {
                    te.visit(this);
                }
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
	
	@Override
	public void meet(StatementPattern node) {
		//stmts.add(node);
	}

}
