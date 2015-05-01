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

public class BGPVisitor extends QueryModelVisitorBase<Exception> {

	protected ArrayList<ArrayList<StatementPattern>> bgps = new ArrayList<ArrayList<StatementPattern>>();
		
	public BGPVisitor() {
		super();
	}

	public ArrayList<ArrayList<StatementPattern>> getBGPs() {
		return bgps;
	}
	
	@Override
	public void meet(Union union) throws Exception {
		super.meet(union);
	}

        @Override
        public void meet(LeftJoin leftjoin) throws Exception {
                super.meet(leftjoin);
        }
	
	@Override
	public void meet(Filter filter)  throws Exception {
		super.meet(filter);
	}
	
	@Override
	public void meet(Service service) throws Exception {
                super.meet(service);
	}

        @Override
        public void meet(Reduced reduced) throws Exception {
                super.meet(reduced);
        }
	
	@Override
	public void meet(Join node) throws Exception {
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
	
	@Override
	public void meet(StatementPattern node) {
		//stmts.add(node);
	}

}
