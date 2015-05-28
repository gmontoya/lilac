//package semLAV;

import com.hp.hpl.jena.sparql.algebra.OpVisitorBase;
import java.util.*;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.algebra.op.OpTriple;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpService;
import com.hp.hpl.jena.sparql.core.BasicPattern;

public class VisitorCountService extends OpVisitorBase {

    int c;

    public VisitorCountService() {
        super();
        c = 0;
    }

    public void visit(OpService opService) {

        c++;
    }


    public int getCount() {
        return c;
    }
}
