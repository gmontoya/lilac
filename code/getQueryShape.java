import java.util.*;
import com.hp.hpl.jena.graph.Triple;

class getQueryShape {

    public static void main(String[] args) {

        String queryFile = args[0];
        ConjunctiveQuery cq = new ConjunctiveQuery(queryFile);
        List<Triple> body = cq.getBody();
        boolean subjectStar = true; 
        boolean objectStar = true;
        Triple t = body.get(0);
        //System.out.println(t);
        for (Triple t0 : body) {
            if (!t.getSubject().equals(t0.getSubject())) {
                subjectStar = false;
                //System.out.println("subject: "+t.getSubject()+" different from subject: "+t0.getSubject());
            }
            if (!t.getObject().equals(t0.getObject())) {                                                                                                                           
                objectStar = false;                 
                //System.out.println("object: "+t.getObject()+" different from object: "+t0.getObject());                                                                                                                              
            }
        }
        if (subjectStar || objectStar) {
            System.out.println("STAR");
        } else {
            List<Triple> tmp = new ArrayList<Triple>();
            tmp.add(t);
            body.remove(t);
            int tmpSize = 1;
            do {
                tmpSize = tmp.size();
                Triple head = tmp.get(0);
                Triple a = null;
                for (Triple t0 : body) {
                    if (t0.getObject().equals(head.getSubject())) {
                        a = t0;
                        break;
                    }
                }
                if (a != null) {
                    tmp.add(0, a);
                    body.remove(a);
                }
                a = null;
                Triple tail = tmp.get(tmp.size()-1);
                for (Triple t0 : body) {              
                    if (t0.getSubject().equals(tail.getObject())) {                                                                                                                
                        a = t0;
                        break;
                    }
                }
                if (a != null) {
                    tmp.add(tmp.size(), a);
                    body.remove(a);
                }
            } while (tmp.size() > tmpSize);
            if (body.size() == 0) {
                System.out.println("PATH");
            } else {
                System.out.println("HYBRID");
            }
        }
    }
}
