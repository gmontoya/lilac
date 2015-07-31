import java.util.*;
import java.io.*;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

class generatePredicateIndex {

    public static void main(String[] args) throws Exception {

        String fragmentsDefinitionFolder = args[0];
        String indexFile = args[1];
        HashMap<String, ArrayList<String>> index = new HashMap<String, ArrayList<String>>();
        processFolder(fragmentsDefinitionFolder, index);
        storeIndex(index, indexFile);
    }

    public static void storeIndex(HashMap<String, ArrayList<String>> index, String fileName) throws Exception {

        BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
                                    new FileOutputStream(fileName), "UTF-8"));
        for (String p : index.keySet()) {
            ArrayList<String> list = index.get(p);
            String s = p;
            for (String e : list) {
                s=s+" "+e;
            }
            output.write(s+"\n");
        }
        output.flush();
        output.close();        
    }

    public static void processFolder(String folder, HashMap<String, ArrayList<String>> index) {

        File f = new File(folder);
        File[] content = f.listFiles();
        if (content != null) {
            for (File g : content) {
                String path = g.getAbsolutePath();
                //System.out.println("path: "+path);
                ConjunctiveQuery cq = new ConjunctiveQuery(path);
                Triple t = cq.getBody().get(0);
                int i = path.lastIndexOf("/") + 1;
                int j = path.lastIndexOf(".");
                j = j < 0 ? path.length() : j;
                String name = path.substring(i, j);
                Node n = t.getPredicate();
                
                if (n.isURI()) {
                    String p = n.getURI();                
                    ArrayList<String> list = index.get(p);
                    if (list == null) {
                        list = new ArrayList<String>();
                    }
                    list.add(name);
                    index.put(p, list);
                }
            }
       }
   }
}
