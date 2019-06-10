package gov.nist.csd.pm.admintool.graph;

import gov.nist.csd.pm.graph.MemGraph;
import gov.nist.csd.pm.graph.model.nodes.Node;

public class SingletonGraph extends MemGraph {
    private static SingletonGraph g;

    //private constructor.
    private SingletonGraph(){
        //Prevent form the reflection api.
        if (g != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    public long getNextID() {
        Long maxId = null;
        for (Node node : g.getNodes()) {
            long id = node.getID();
            if (maxId == null) {
                maxId = id;
            } else {
                if (maxId < id) maxId = id;
            }
        }
        if (maxId == null) {
            return 0;
        }
        return maxId + 1;
    }

    public synchronized static SingletonGraph getInstance(){
        if (g == null){ //if there is no instance available... create new one
            g = new SingletonGraph();
        }
        return g;
    }

    public synchronized static SingletonGraph updateGraph(SingletonGraph graph) {
        g = graph;
        return g;
    }
}
