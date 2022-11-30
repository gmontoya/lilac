package com.fluidops.fedx.optimizer;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.Service;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

public class StatementsVisitor extends QueryModelVisitorBase<Exception> {

	protected List<StatementPattern> stmts = new ArrayList<StatementPattern>();
		
	public StatementsVisitor() {
		super();
	}

	public List<StatementPattern> getStatements() {
		return stmts;
	}
	
	@Override
	public void meet(Union union) throws Exception {
		super.meet(union);
	}
	
	@Override
	public void meet(Filter filter)  throws Exception {
		super.meet(filter);
	}
	
	@Override
	public void meet(Service service) {
	}
	
	@Override
	public void meet(Join node) throws Exception {
	        super.meet(node);	
	}
	
	@Override
	public void meet(StatementPattern node) {
		stmts.add(node);
	}

}
