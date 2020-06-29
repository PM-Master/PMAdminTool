package gov.nist.csd.pm.admintool.graph;

import gov.nist.csd.pm.epp.EPPOptions;
import gov.nist.csd.pm.epp.events.EventContext;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.operations.OperationSet;
import gov.nist.csd.pm.pap.PAP;
import gov.nist.csd.pm.pdp.PDP;
import gov.nist.csd.pm.pdp.services.GraphService;
import gov.nist.csd.pm.pdp.services.UserContext;
import gov.nist.csd.pm.pip.graph.Graph;
import gov.nist.csd.pm.pip.graph.MemGraph;
import gov.nist.csd.pm.pip.graph.model.nodes.Node;
import gov.nist.csd.pm.pip.graph.model.nodes.NodeType;
import gov.nist.csd.pm.pip.graph.mysql.MySQLConnection;
import gov.nist.csd.pm.pip.graph.mysql.MySQLGraph;
import gov.nist.csd.pm.pip.obligations.MemObligations;
import gov.nist.csd.pm.pip.obligations.model.Obligation;
import gov.nist.csd.pm.pip.prohibitions.MemProhibitions;
import gov.nist.csd.pm.pip.prohibitions.mysql.MySQLProhibitions;

import java.util.*;

/**
 * The "In-Memory" graph used throughout the entirety of the admin tool.
 *
 * There is only one instance which can be retrieved using SingletonGraph.getInstance().
 *
 * It also implies the super context, so I have created "wrapper" methods which use the super
 * context automatically.
 */
public class SingletonGraph extends PDP {
    private Boolean isMysql;
    private static SingletonGraph g; // the single instance
    private static UserContext superContext;
    private static String superPCId, superUAId, superOAId;
    private static Random rand;
    private static Set<PolicyClassWithActive> activePCs;
    private static MySQLConnection connection = new MySQLConnection();

    private SingletonGraph(PAP pap) throws PMException {
        super(pap, new EPPOptions(), new OperationSet());

        //Prevent form the reflection api.
        if (g != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    public Boolean getMysql() {
        return isMysql;
    }

    public void setMysql(Boolean mysql) {
        isMysql = mysql;
    }

    /**
     * Gets the singleton instance of this class
     */
    public synchronized static SingletonGraph getInstance() {
        rand = new Random();
        if (g == null) { // if there is no instance available... create new one
            fixGraphData(new MySQLGraph(connection));
            g.setMysql(true);
        }
        return g;
    }

    private synchronized static void fixGraphData(Graph graph) {
        try {
                g = new SingletonGraph(new PAP(
                        graph,
                        new MySQLProhibitions(connection),
                        new MemObligations()
                ));
            System.out.println("MySQLGraph");
            superContext = null;
            activePCs = new HashSet<>();

            for (Node n : graph.getNodes()) {;
                    if (n.getProperties().get("namespace") != null && n.getProperties().get("namespace").equals("super")) {
                        switch (n.getType()) {
                            case OA:
                                System.out.println("Super OA: " + n.getName());
                                superOAId = n.getName();
                                break;
                            case UA:
                                if (n.getName().equals("super_ua2")) {
                                    System.out.println("Super UA: " + n.getName());
                                    superUAId = n.getName();
                                }
                                break;
                            case U:
                                System.out.println("Super U: " + n.getName());
                                superContext = new UserContext(n.getName(), rand.toString());
                                break;
                            case PC:
                                System.out.println("Super PC: " + n.getName());
                                superPCId = n.getName();
                                break;
                    }
               }

                if (n.getType().equals(NodeType.PC)) {
                    activePCs.add(new PolicyClassWithActive(n));
                }
            }
        } catch (PMException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private synchronized static void fixGraphDataMem(Graph graph) {
        try {
            g = new SingletonGraph(new PAP(
                    graph,
                    new MemProhibitions(),
                    new MemObligations()
            ));
            System.out.println("MemGraph");
            superContext = null;
            activePCs = new HashSet<>();
            for (Node n : graph.getNodes()) {
                if (n.getProperties().get("namespace") != null && n.getProperties().get("namespace").equals("super")) {
                    switch (n.getType()) {
                        case OA:
                            System.out.println("Super OA: " + n.getName());
                            superOAId = n.getName();
                            break;
                        case UA:
                            if (n.getName().equals("super_ua2")) {
                                System.out.println("Super UA: " + n.getName());
                                superUAId = n.getName();
                            }
                            break;
                        case U:
                            System.out.println("Super U: " + n.getName());
                            superContext = new UserContext(n.getName(), rand.toString());
                            break;
                        case PC:
                            System.out.println("Super PC: " + n.getName());
                            superPCId = n.getName();
                            break;
                    }
                }

                if (n.getType().equals(NodeType.PC)) {
                    activePCs.add(new PolicyClassWithActive(n));
                }
            }
            //System.out.println(superPCId);
        } catch (PMException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static UserContext getSuperContext() {
        return superContext;
    }

    public static String getSuperPCId() {
        return superPCId;
    }

    public static String getSuperOAId() {
        return superOAId;
    }

    public static String getSuperUAId() {
        return superUAId;
    }

    public static Set<PolicyClassWithActive> getActivePCs() {
        return activePCs;
    }


    public String toString(){
        List<String> pcs = new ArrayList<>();
        for (PolicyClassWithActive pc: activePCs) {
            if (pc.isActive()){
                pcs.add(pc.name);
            }
        }
            return "Active pcs: " + pcs.toString();
    }

    public Node createPolicyClass(String name, Map<String, String> properties) throws PMException {

        if (superContext != null) {
            Node newPC = g.getGraphService(superContext).createPolicyClass(name, properties);
            activePCs.add(new PolicyClassWithActive(newPC));
            return newPC;
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public SingletonGraph updateGraph (boolean isMySQL){
        g = null;
        if (isMySQL) {
            fixGraphData(new MySQLGraph(connection));
            g.setMysql(true);
        } else {
            fixGraphDataMem(new MemGraph());
            g.setMysql(false);
        }
        return g;
    }

    // wrapped methods (implies super context) \\
    // graph service methods
    public void reset() throws PMException {
        g.getGraphService(superContext).reset(superContext);
    }

    public Node createNode(String name, NodeType type, Map<String, String> properties, String parent) throws PMException {
        if (superContext != null) {
            return g.getGraphService(superContext).createNode(name, type, properties, parent );
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public String getPolicyClassDefault(String pc, NodeType type) throws PMException {
        if (pc.equalsIgnoreCase("0")) {
            if (superContext != null) {
                return g.getGraphService(superContext).getPolicyClassDefault(superPCId, type);
            } else {
                throw new PMException("Super Context is Null");
            }
        } else return g.getGraphService(superContext).getPolicyClassDefault(pc, type);
    }

    public void updateNode(String name, Map<String, String> properties) throws PMException {
        if (superContext != null) {
            g.getGraphService(superContext).updateNode(name, properties);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public void deleteNode(String name) throws PMException {
        if (superContext != null) {
            g.getGraphService(superContext).deleteNode(name);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public boolean exists(String name) throws PMException {
        if (superContext != null) {
            return g.getGraphService(superContext).exists(name);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public Set<Node> getNodes() throws PMException {
        if (superContext != null) {
            return g.getGraphService(superContext).getNodes();
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public Set<String> getPolicies() throws PMException {
        if (superContext != null) {
            return g.getGraphService(superContext).getPolicyClasses();
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public Set<String> getChildren(String name) throws PMException {
        if (superContext != null) {
            return g.getGraphService(superContext).getChildren(name);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public Set<String> getParents(String node) throws PMException {
        if (superContext != null) {
            return g.getGraphService(superContext).getParents(node);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public void assign(String child, String parent) throws PMException {
        if (superContext != null) {
            g.getGraphService(superContext).assign(child, parent);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public void deassign(String child, String parent) throws PMException {
        if (superContext != null) {
            g.getGraphService(superContext).deassign(child, parent);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public void associate(String ua, String target, OperationSet operations) throws PMException {
        if (superContext != null) {
            g.getGraphService(superContext).associate(ua, target, operations);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public void dissociate(String ua, String target) throws PMException {
        if (superContext != null) {
            g.getGraphService(superContext).dissociate(ua, target);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public Map<String, OperationSet> getSourceAssociations(String source) throws PMException {
        if (superContext != null) {
            return g.getGraphService(superContext).getSourceAssociations(source);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public Map<String, OperationSet> getTargetAssociations(String target) throws PMException {
        if (superContext != null) {
            return g.getGraphService(superContext).getTargetAssociations(target);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public Set<Node> search(String name, String type, Map<String, String> properties) throws PMException {
        if (superContext != null) {
            return g.getGraphService(superContext).search(NodeType.toNodeType(type), properties);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public Node getNode(String name) throws PMException {
        if (superContext != null) {
            return g.getGraphService(superContext).getNode(name);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

/*    public HashSet<PolicyClassWithActive> getActivePcs () {
        //return the active pcs within the graph
    }*/

    public void processEvent (EventContext eventCtx) throws PMException {
        if (superContext != null) {
            g.getEPP().processEvent(eventCtx);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    // obligation service methods
    public void addObl(Obligation obligation) throws PMException {
        if (superContext != null) {
            g.getObligationsService(superContext).add(obligation, true);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public Obligation getObl(String label) throws PMException {
        if (superContext != null) {
            return g.getObligationsService(superContext).get(label);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public List<Obligation> getAllObls() throws PMException {
        if (superContext != null) {
            return g.getObligationsService(superContext).getAll();
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public void updateObl(String label, Obligation obligation) throws PMException {
        if (superContext != null) {
            g.getObligationsService(superContext).update(label, obligation);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public void deleteObl(String label) throws PMException {
        if (superContext != null) {
            g.getObligationsService(superContext).delete(label);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public void enableObl(String label) throws PMException {
        if (superContext != null) {
            g.getObligationsService(superContext).setEnable(label, true);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public List<Obligation> getEnabledObls() throws PMException {
        if (superContext != null) {
            return g.getObligationsService(superContext).getEnabled();
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    /*public void resetAllObls () throws PMException {
        if (superContext != null) {
            g.getObligationsService(superContext).reset(superContext);
        } else {
            throw new PMException("Super Context is Null");
        }
    }*/
    // ENDOF wrapped methods (implies super context) \\

    // Policy class with active feild class
    public static class PolicyClassWithActive {
        Node pc; // reference to actual pc node
        String name;
        boolean active;

        public PolicyClassWithActive() {
            pc = null;
            name = null;
            active = true;
        }

        public PolicyClassWithActive(Node pc) {
            this.pc = pc;
            name = pc.getName();
            this.active = true;
        }

        public PolicyClassWithActive(Node pc, boolean active) {
            this.pc = pc;
            name = pc.getName();
            this.active = active;
        }

        public String getName() {
            return name;
        }

        public PolicyClassWithActive setName(String name) {
            this.name = name;
            return this;
        }

        public Node getPC() {
            return pc;
        }

        public PolicyClassWithActive setPC(Node pc) {
            this.pc = pc;
            return this;
        }

        public boolean isActive() {
            return active;
        }

        public PolicyClassWithActive setActive(boolean active) {
            this.active = active;
            return this;
        }
    }
}
