package gov.nist.csd.pm.admintool.graph;

import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.pap.PAP;
import gov.nist.csd.pm.pdp.*;
import gov.nist.csd.pm.pdp.services.UserContext;
import gov.nist.csd.pm.pip.graph.MemGraph;
import gov.nist.csd.pm.pip.graph.model.nodes.Node;
import gov.nist.csd.pm.pip.graph.model.nodes.NodeType;
import gov.nist.csd.pm.pip.obligations.MemObligations;
import gov.nist.csd.pm.pip.prohibitions.MemProhibitions;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SingletonGraph extends PDP {
    private static SingletonGraph g;
    private static UserContext superContext;
    private static long superPCId, superUAId, superOAId;

    //private constructor.
    private SingletonGraph(PAP pap){
        super(pap);
//        try {
//            getGraphService().createNode(new UserContext(-1, -1),
//                    -1, "Super PC", NodeType.PC, null);
//        } catch (PMException e) {
//            System.out.println(e.getMessage());
//            e.printStackTrace();
//        }
        //Prevent form the reflection api.
        if (g != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    public long getNextID() {
        Long maxId = null;
        Set<Node> nodes = new HashSet<>();
        try {
            nodes = g.getGraphService().getNodes(superContext);
        } catch (PMException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        for (Node node : nodes) {
            long id = node.getID();
            if (maxId == null) {
                maxId = id;
            } else {
                if (maxId < id) maxId = id;
            }
        }
        if (maxId == null || maxId == -1.0) {
            return 1;
        }
        return maxId + 1;
    }

    public synchronized static SingletonGraph getInstance() {
        if (g == null){ // if there is no instance available... create new one
            try {
                g = new SingletonGraph(new PAP(
                        new MemGraph(),
                        new MemProhibitions(),
                        new MemObligations()
                ));
                long superId = -1;
                for (Node n : g.getPAP().getGraphPAP().getNodes()) {
                    if (n.getProperties().get("namespace") == "super") {
                        switch (n.getType()) {
                            case OA:
                                System.out.println("Super OA: " + n.getID());
                                superOAId = n.getID();
                                break;
                            case UA:
                                if (n.getName().equals("super_ua2")) {
                                    System.out.println("Super UA: " + n.getID());
                                    superUAId = n.getID();
                                }
                                break;
                            case U:
                                System.out.println("Super U: " + n.getID());
                                superId = n.getID();
                                break;
                            case PC:
                                System.out.println("Super PC: " + n.getID());
                                superPCId = n.getID();
                                break;
                        }
                    }
                }
                superContext = new UserContext(superId, -1);
//                System.out.println(superPCId);
            } catch (PMException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        return g;
    }

    public synchronized static SingletonGraph updateGraph(SingletonGraph graph) {
        g = graph;
        superContext = null;
        return g;
    }

    private static UserContext getSuperContext() {
        return superContext;
    }

    public static long getSuperPCId() {
        return superPCId;
    }

    public static long getSuperOAId() {
        return superOAId;
    }

    public static long getSuperUAId() {
        return superUAId;
    }


    // wrapped from graph service methods (implies super context) \\
    public void reset() throws PMException {
        g.getGraphService().reset(superContext);
    }

    public Node createNode(long parentID, String name, NodeType type, Map<String, String> properties) throws PMException {
        if (superContext != null) {
            return g.getGraphService().createNode(superContext, parentID, name, type, properties);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public long getPolicyClassDefault(NodeType type) throws PMException {
        if (superContext != null) {
            return g.getGraphService().getPolicyClassDefault(superPCId, type);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public void updateNode(long id, String name, Map<String, String> properties) throws PMException {
        if (superContext != null) {
            g.getGraphService().updateNode(superContext, id, name, properties);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public void deleteNode(long nodeID) throws PMException {
        if (superContext != null) {
            g.getGraphService().deleteNode(superContext, nodeID);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public boolean exists(long nodeID) throws PMException {
        if (superContext != null) {
            return g.getGraphService().exists(superContext, nodeID);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public Set<Node> getNodes() throws PMException {
        if (superContext != null) {
            return g.getGraphService().getNodes(superContext);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public Set<Long> getPolicies() throws PMException {
        if (superContext != null) {
            return g.getGraphService().getPolicies(superContext);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public Set<Node> getChildren(long nodeID) throws PMException {
        if (superContext != null) {
            return g.getGraphService().getChildren(superContext, nodeID);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public Set<Node> getParents(long nodeID) throws PMException {
        if (superContext != null) {
            return g.getGraphService().getParents(superContext, nodeID);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public void assign(long childID, long parentID) throws PMException {
        if (superContext != null) {
            g.getGraphService().assign(superContext, childID, parentID);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public void deassign(long childID, long parentID) throws PMException {
        if (superContext != null) {
            g.getGraphService().deassign(superContext, childID, parentID);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public void associate(long uaID, long targetID, Set<String> operations) throws PMException {
        if (superContext != null) {
            g.getGraphService().associate(superContext, uaID, targetID, operations);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public void dissociate(long uaID, long targetID) throws PMException {
        if (superContext != null) {
            g.getGraphService().dissociate(superContext, uaID, targetID);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public Map<Long, Set<String>> getSourceAssociations(long sourceID) throws PMException {
        if (superContext != null) {
            return g.getGraphService().getSourceAssociations(superContext, sourceID);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public Map<Long, Set<String>> getTargetAssociations(long targetID) throws PMException {
        if (superContext != null) {
            return g.getGraphService().getTargetAssociations(superContext, targetID);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public Set<Node> search(String name, String type, Map<String, String> properties) throws PMException {
        if (superContext != null) {
            return g.getGraphService().search(superContext, name, type, properties);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public Node getNode(long id) throws PMException {
        if (superContext != null) {
            return g.getGraphService().getNode(superContext, id);
        } else {
            throw new PMException("Super Context is Null");
        }
    }
}
