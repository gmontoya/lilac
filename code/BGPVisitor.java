
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

public class BGPVisitor extends OpVisitorBase {

	protected ArrayList<ArrayList<Triple>> bgps = new ArrayList<ArrayList<Triple>>();
		
	public BGPVisitor() {
		super();
	}

	public ArrayList<ArrayList<Triple>> getBGPs() {
		return bgps;
	}
	
	@Override
	public void visit(OpUnion union) {
		super.visit(union);
	}

        @Override
        public void visit(OpLeftJoin leftjoin) {
                super.visit(leftjoin);
        }
	
	@Override
	public void visit(OpFilter filter) {
		super.visit(filter);
	}
	
	@Override
	public void visit(OpService service) {
                super.visit(service);
	}

        @Override
        public void visit(OpReduced reduced) {
                super.visit(reduced);
        }
	
	@Override
	public void visit(OpJoin node) {
            super.visit(node);
        }

        public void visit(OpBGP node) {
            ArrayList<Triple> es = new ArrayList<Triple>();
            BasicPattern bp = node.getPattern();
            Iterator<Triple> it = bp.iterator();
            while (it.hasNext()) {
                Triple t = it.next();
                es.add(t);
            }
            bgps.add(es);
        }	
	@Override
	public void visit(OpTriple node) {
		//stmts.add(node);
	}

    public static void main (String[] args) {

        String queryIn = args[0];
        try {
            Query q = QueryFactory.read(queryIn);
            Op op = (new AlgebraGenerator()).compile(q);
            BGPVisitor bgpv = new BGPVisitor();
            OpWalker.walk(op, bgpv);
            ArrayList<ArrayList<Triple>> bgps = bgpv.getBGPs();
            for (ArrayList<Triple> bgp : bgps) {
                System.out.print(bgp);
            }
        } catch (com.hp.hpl.jena.query.QueryParseException e) {
            System.out.println("error while reading "+queryIn);
        }
    }
}
