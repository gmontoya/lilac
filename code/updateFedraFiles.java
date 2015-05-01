import java.util.*;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import java.io.*;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.Node;

public class updateFedraFiles {

    public static void main2(String [] args) throws Exception {
        String viewsFolder = args[0];
        String viewName1 = args[1];
        HashMap<String,Query> views = loadViews(viewsFolder);
        //String containedInViewFile = args[2];
        //HashMap<String, HashSet<String>> containedInView = new HashMap<String, HashSet<String>>();
        //loadViewContainments(containedInViewFile, containedInView);
        Query v1= views.get(viewName1);
        for (String viewName2 : views.keySet()) {
             Query v2 = views.get(viewName2);
             if (startFederation.containedIn(v1, v2)) {
             //if (containedInView.containsKey(viewName1) && containedInView.get(viewName1).contains(viewName2)) {
                 System.out.println(viewName2+" is a superview of "+viewName1);
             }
             if (startFederation.containedIn(v2, v1)) {
             //if (containedInView.containsKey(viewName2) && containedInView.get(viewName2).contains(viewName1)) {
                 System.out.println(viewName2+" is a subview of "+viewName1);
             }
        }
    }

    public static String findName(HashMap<String,Query> views, Query v, HashMap<String,String> datasets, String ds) {

        for (String wn : views.keySet()) {
            //System.out.println("considering view "+wn);
            //System.out.println("its dataset is: "+datasets.get(wn)); 
            Query w = views.get(wn);
            if (datasets.get(wn).equals(ds) && exactMatch(v, w)) {
                return wn;
            }
        }
        return null;
    }

    public static void main(String [] args) throws Exception {
        String viewDefinitionFile = args[0];
        String viewsFolder = args[1];
        String datasetsFile = args[2];
        String endpoint = args[3];
        String publicEndpoint = args[4];
        String file = args[5];
        Query v = QueryFactory.read(viewDefinitionFile);
        HashMap<String,Query> views = loadViews(viewsFolder);
        HashMap<String,String> datasets = loadDatasets(datasetsFile);
        long t =  System.currentTimeMillis();
        //System.out.println("there are "+datasets.keySet().size()+" elements in datasets:\n"+datasets.toString());
        String viewName = findName(views, v, datasets, publicEndpoint);
        t = System.currentTimeMillis() - t;
        if (viewName==null) {
            viewName = "view"+(views.size()+1);
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                                          new FileOutputStream(viewsFolder+"/"+viewName), "UTF-8"));
            out.write(v.toString());
            out.flush();
            out.close();
            out = new BufferedWriter(new OutputStreamWriter(
                                          new FileOutputStream(datasetsFile, true), "UTF-8"));
            out.write(viewName+" "+publicEndpoint+"\n");
            out.flush();
            out.close();
        }
        append(file, t+"");
        System.out.println(viewName);
    }

    public static boolean exactMatch(Query q1, Query q2) {
        ConjunctiveQuery cq1 = new ConjunctiveQuery(q1, "cq1");
        List<Triple> ts1 = cq1.getBody();
        Triple t1 = ts1.get(0);
        Node s1 = t1.getSubject();
        Node p1 = t1.getPredicate();
        Node o1 = t1.getObject();
        ConjunctiveQuery cq2 = new ConjunctiveQuery(q2, "cq2");
        List<Triple> ts2 = cq2.getBody();
        Triple t2 = ts2.get(0);
        Node s2 = t2.getSubject();
        Node p2 = t2.getPredicate();
        Node o2 = t2.getObject();
        return exactMatch(s1,s2) && exactMatch(p1,p2) && exactMatch(o1,o2);
    }

    public static boolean exactMatch(Node n1, Node n2) {
        boolean b = (n1.isVariable() && n2.isVariable()); 
        b = b || (!n1.isVariable() && !n2.isVariable() && n1.equals(n2));
        return b;
    }

    public static void append(String fileName, String msg) throws Exception {

        BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
                                    new FileOutputStream(fileName, true), "UTF-8"));
        output.write(msg+"\n");
        output.flush();
        output.close();
    }

    public static void obtainNewEndpointContainments(Query v1, String viewName1, HashMap<String,Query>  views, HashMap<String, HashSet<String>> containedInView, String e1, String pe, HashMap<String, HashMap<String, HashSet<String>>> containedInEndpoint) {
         //System.err.println("adding view "+viewName1+" in endpoint "+e1);
         for (String viewName2 : views.keySet()) {
             Query v2 = views.get(viewName2);
             if (containedInView.containsKey(viewName1) && containedInView.get(viewName1).contains(viewName2)) {
                 for (String endpoint : containedInEndpoint.keySet()) {
                     // for the endpoints that already offered v2
                     if (containedInEndpoint.get(endpoint).containsKey(viewName2)) {
                         HashSet<String> os = containedInEndpoint.get(endpoint).get(viewName2);
                         HashSet<String> nos = new HashSet<String>();
                         nos.addAll(os);
                         HashMap<String, HashSet<String>> aux = containedInEndpoint.get(endpoint);
                         if (containedInEndpoint.get(endpoint).containsKey(viewName1)) {
                             nos.addAll(containedInEndpoint.get(endpoint).get(viewName1));
                         }
                         aux.put(viewName1, nos);
                         // now they also offer v1 with the same origin than v2
                         containedInEndpoint.put(endpoint, aux);
                         //System.err.println("now endpoint "+endpoint+" offers view "+viewName1+" coming from "+nos);
                         /* for (String source : os) {
                             if (containedInEndpoint.get(source).containsKey(viewName2) && !containedInEndpoint.get(source).containsKey(viewName1)) {
                                 HashSet<String> nnos = new HashSet<String>();
                                 nnos.addAll(containedInEndpoint.get(source).get(viewName2));
                                 HashMap<String, HashSet<String>> aux2 = containedInEndpoint.get(source);
                                 aux2.put(viewName1, nnos);
                                 containedInEndpoint.put(source, aux2);
                             }
                         }*/
                     }
                 }
             }
             if (containedInView.containsKey(viewName2) && containedInView.get(viewName2).contains(viewName1)) {
                 HashMap<String, HashSet<String>> aux = containedInEndpoint.get(e1);
                 if (aux == null) {
                     aux = new HashMap<String, HashSet<String>>();
                 }
                 HashSet<String> os = new HashSet<String>();
                 if (aux.containsKey(viewName2)) {
                     os.addAll(aux.get(viewName2));
                 }
                 os.addAll(aux.get(viewName1));
                 aux.put(viewName2, os);
                 containedInEndpoint.put(e1, aux);
                 //System.err.println("now endpoint "+e1+" offers view "+viewName2+" coming from "+os);
                 aux = containedInEndpoint.get(pe);
                 if (aux == null) {
                     aux = new HashMap<String, HashSet<String>>();
                 }
                 os = new HashSet<String>();
                 if (aux.containsKey(viewName2)) {
                     os.addAll(containedInEndpoint.get(pe).get(viewName2));
                 }
                 os.addAll(containedInEndpoint.get(pe).get(viewName1));
                 aux.put(viewName2, os);
                 containedInEndpoint.put(pe, aux);
                 //System.err.println("now endpoint "+pe+" offers view "+viewName2+" coming from "+os);
             }
         }
    }

    public static void obtainNewViewContainments(Query v1, String viewName1, HashMap<String,Query>  views, HashMap<String, HashSet<String>> containedInView, String e1, String pe, HashMap<String, HashMap<String, HashSet<String>>> containedInEndpoint) {
         // for all the views already known
         for (String viewName2 : views.keySet()) {
             Query v2 = views.get(viewName2);
             // such that the new view v1 is contained in them
             if (startFederation.containedIn(v1, v2)) {
                 HashSet<String> origins = new HashSet<String>();
                 if (containedInView.containsKey(viewName1)) {
                     origins = containedInView.get(viewName1);
                 } else {
                     origins = new HashSet<String>();
                 }
                 // add v2 in their origins
                 origins.add(viewName2);
                 containedInView.put(viewName1, origins);
             }
             if (startFederation.containedIn(v2, v1)) {
                 HashSet<String> origins = new HashSet<String>();
                 if (containedInView.containsKey(viewName2)) {
                     origins = containedInView.get(viewName2);
                 } else {
                     origins = new HashSet<String>();
                 }
                 origins.add(viewName1);
                 containedInView.put(viewName2, origins);
             }
         }
    }

    public static void complete(HashMap<String, HashMap<String, HashSet<String>>> containedInEndpoint, String endpoint, String publicEndpoint, String view) {

        complete0(containedInEndpoint, endpoint, publicEndpoint, view);
        complete0(containedInEndpoint, publicEndpoint, endpoint, view);
    }

    public static void complete0(HashMap<String, HashMap<String, HashSet<String>>> containedInEndpoint, String endpoint1, String endpoint2, String view) {

        HashMap<String, HashSet<String>> viewOrigins = null;
        if (containedInEndpoint.containsKey(endpoint1)) {
            viewOrigins = containedInEndpoint.get(endpoint1);
        } else {
            viewOrigins = new HashMap<String, HashSet<String>>();
        }
        HashSet<String> origins = null;
        if (viewOrigins.containsKey(view)) {
            origins = viewOrigins.get(view);
        } else {
            origins = new HashSet<String>();
        }
        origins.add(endpoint2);
        viewOrigins.put(view, origins);
        containedInEndpoint.put(endpoint1, viewOrigins);
    }


    public static HashMap<String,Query> loadViews(String viewsFolder) {
        File f = new File(viewsFolder);
        File[] content = f.listFiles();
        HashMap<String, Query> hs = new HashMap<String, Query>();
        if (content != null) {
            for (File g : content) {
                    String path = g.getAbsolutePath();
                    try {
                        Query v = QueryFactory.read(path);
                        int i = path.lastIndexOf("/");
                        int j = path.lastIndexOf(".");
                        j = j < 0 ? path.length() : j;
                        String name = path.substring(i+1, j);
                        hs.put(name, v);
                     } catch (Exception e) {
                        System.err.println("error reading view: "+path);
                     }
            }
        }
        return hs;
    }

    public static void updateViewContainments(String v, HashMap<String, HashSet<String>> containedIn, String file) throws Exception {

        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                                                      new FileOutputStream(file, true), "UTF-8"));
        for (String v1 : containedIn.keySet()) {
            HashSet<String> sv = containedIn.get(v1);
            for (String v2 : sv) {
                out.write(v1+" "+v2+"\n");
            }
        }
        out.write(v+" "+v+"\n");
        out.flush();
        out.close();
    }

    public static HashMap<String,String> loadDatasets(String file) {

        HashMap<String,String> map = new HashMap<String,String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String l = br.readLine();
            while (l!=null) {
                StringTokenizer st = new StringTokenizer(l);
                if (st.hasMoreTokens()) {
                    String fragment = st.nextToken();
                    String dataset = st.nextToken();
                    map.put(fragment,dataset);
                }
                l = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            System.err.println("Problems reading file: "+file);
            System.exit(1);
        }
        return map;
    }

    public static void loadEndpointContainment (String file, HashMap<String, HashMap<String, HashSet<String>>> containedInEndpoint) {

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String l = br.readLine();
            while (l!=null) {
                StringTokenizer st = new StringTokenizer(l);
                if (st.hasMoreTokens()) {
                    String endpoint = st.nextToken();
                    HashMap<String, HashSet<String>> os = containedInEndpoint.get(endpoint);
                    if (os == null) {
                        os = new HashMap<String, HashSet<String>>();
                    }

                    if (st.hasMoreTokens()) {
                        String view = st.nextToken();
                        HashSet<String> es = os.get(view);
                        if (es == null) {
                            es = new HashSet<String>();
                        }
                        while (st.hasMoreTokens()) {
                            String endpointSource = st.nextToken();
                            es.add(endpointSource);
                        }
                        os.put(view, es);
                    }

                    containedInEndpoint.put(endpoint, os);
                }
                l = br.readLine();
            }
            br.close();

        } catch (IOException e) {
            System.err.println("Problems reading file: "+file);
            System.exit(1);
        }
    }
}

