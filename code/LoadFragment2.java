import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest; 
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.Update;
import com.hp.hpl.jena.sparql.modify.request.UpdateLoad;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.*;
import java.util.concurrent.CountDownLatch;
import com.hp.hpl.jena.update.Update;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.sparql.modify.request.UpdateDataInsert;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.modify.request.QuadDataAcc;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;

class LoadFragment2 {

   public static void main(String[] args) throws Exception {

       String fragmentFile = args[0];
       String fusekiEndpoint = args[1];
       Model model = ModelFactory.createDefaultModel();
       model.read(fragmentFile);
       StmtIterator it = model.listStatements();
       
       UpdateRequest ur = new UpdateRequest();
       while (it.hasNext()) {
           QuadDataAcc triples = new QuadDataAcc();
           int i = 0;
           for (i = 0; i < 100 && it.hasNext(); i++) {
               Triple t = it.next().asTriple();
               //System.out.println("triple: "+t);
               triples.addTriple(t);
           }
           System.out.println("Including update instruction with "+i+" triples");
           Update u = new UpdateDataInsert(triples);
           ur.add(u);
       }
       System.out.println("creating remote");      
       UpdateProcessor up = UpdateExecutionFactory.createRemote(ur, fusekiEndpoint);
       System.out.println("created "+up.getClass());
       up.execute();
       System.out.println("executed");
       it.close();
       System.out.println("iterator closed");
       model.close();
       System.out.println("model closed");
   }
}
