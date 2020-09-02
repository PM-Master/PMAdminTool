package gov.nist.csd.pm.admintool.graph;

import gov.nist.csd.pm.epp.EPPOptions;
import gov.nist.csd.pm.epp.events.EventContext;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.operations.OperationSet;
import gov.nist.csd.pm.operations.Operations;
import gov.nist.csd.pm.pap.GraphAdmin;
import gov.nist.csd.pm.pap.ObligationsAdmin;
import gov.nist.csd.pm.pap.PAP;
import gov.nist.csd.pm.pap.ProhibitionsAdmin;
import gov.nist.csd.pm.pdp.PDP;
import gov.nist.csd.pm.pdp.services.UserContext;
import gov.nist.csd.pm.pip.graph.Graph;
import gov.nist.csd.pm.pip.graph.MemGraph;
import gov.nist.csd.pm.pip.graph.model.nodes.Node;
import gov.nist.csd.pm.pip.graph.model.nodes.NodeType;
import gov.nist.csd.pm.pip.graph.mysql.MySQLConnection;
import gov.nist.csd.pm.pip.graph.mysql.MySQLGraph;
import gov.nist.csd.pm.pip.obligations.MemObligations;
import gov.nist.csd.pm.pip.obligations.evr.EVRParser;
import gov.nist.csd.pm.pip.obligations.model.Obligation;
import gov.nist.csd.pm.pip.prohibitions.MemProhibitions;
import gov.nist.csd.pm.pip.prohibitions.model.Prohibition;
import gov.nist.csd.pm.pip.prohibitions.mysql.MySQLProhibitions;

import java.io.InputStream;
import java.util.*;

/**
 * The "In-Memory" graph used throughout the entirety of the admin tool.
 *
 * There is only one instance which can be retrieved using SingletonGraph.getInstance().
 *
 * It also implies the super context, so I have created "wrapper" methods which use the super
 * context automatically.
 */
public class SingletonGraph {
    private static Boolean isMysql;
    private static SingletonGraph g; // the single instance
    private static PDP pdp;
    private static UserContext superContext;
    private static String superPCId, superUAId, superOAId;
    private static Random rand;
    private static Set<PolicyClassWithActive> activePCs;
    private static MySQLConnection connection = new MySQLConnection();

    private SingletonGraph(PAP pap) throws PMException {
        pdp = PDP.newPDP(pap, new EPPOptions(), new OperationSet());

        //Prevent form the reflection api.
//        if (pdp == null){
//            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
//        }
    }

    public synchronized static PDP getPDP() {
        return getInstance().pdp;
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
            fixGraphDataMem(new MemGraph());
            g.setMysql(false);
        }
        return g;
    }

    private synchronized static void fixGraphData(Graph graph) {
        try {
                g = new SingletonGraph(new PAP(
                        new GraphAdmin(graph),
                        new ProhibitionsAdmin(new MySQLProhibitions(connection)),
                        new ObligationsAdmin(new MemObligations())
                ));
            System.out.println("MySQLGraph");
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

            OperationSet resourceOps = new OperationSet();
//            resourceOps.add(Operations.ALL_OPS);
//            resourceOps.add(Operations.ALL_RESOURCE_OPS);
            resourceOps.add(Operations.READ);
            resourceOps.add(Operations.WRITE);
            resourceOps.add(Operations.OBJECT_ACCESS);
            getPDP().setResourceOps(resourceOps);

        } catch (PMException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private synchronized static void fixGraphDataMem(Graph graph) {
        try {
            g = new SingletonGraph(new PAP(
                    new GraphAdmin(graph),
                    new ProhibitionsAdmin(new MemProhibitions()),
                    new ObligationsAdmin(new MemObligations())
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

            OperationSet resourceOps = new OperationSet();
//            resourceOps.add(Operations.ALL_OPS);
//            resourceOps.add(Operations.ALL_RESOURCE_OPS);
            resourceOps.add(Operations.READ);
            resourceOps.add(Operations.WRITE);
            resourceOps.add(Operations.OBJECT_ACCESS);
            //g.setResourceOps(resourceOps);
            getPDP().setResourceOps(resourceOps);

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

    public static void resetActivePCs() {
        activePCs.removeIf(policyClassWithActive -> !policyClassWithActive.getName().equals("super_pc"));
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

    public static Node createPolicyClass(String name, Map<String, String> properties) throws PMException {

        if (superContext != null) {
            Node newPC = getPDP().getGraphService(superContext).createPolicyClass(name, properties);
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
        getPDP().getGraphService(superContext).reset(superContext);
    }

    public Node createNode(String name, NodeType type, Map<String, String> properties, String parent) throws PMException {
        if (superContext != null) {
            return getPDP().getGraphService(superContext).createNode(name, type, properties, parent );
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public String getPolicyClassDefault(String pc, NodeType type) throws PMException {
        if (pc.equalsIgnoreCase("0")) {
            if (superContext != null) {
                return getPDP().getGraphService(superContext).getPolicyClassDefault(superPCId, type);
            } else {
                throw new PMException("Super Context is Null");
            }
        } else return getPDP().getGraphService(superContext).getPolicyClassDefault(pc, type);
    }

    public void updateNode(String name, Map<String, String> properties) throws PMException {
        if (superContext != null) {
            getPDP().getGraphService(superContext).updateNode(name, properties);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public void deleteNode(String name) throws PMException {
        if (superContext != null) {
            getPDP().getGraphService(superContext).deleteNode(name);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public boolean exists(String name) throws PMException {
        if (superContext != null) {
            return getPDP().getGraphService(superContext).exists(name);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public Set<Node> getNodes() throws PMException {
        if (superContext != null) {
            return getPDP().getGraphService(superContext).getNodes();
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public Set<Node> getActiveNodes() throws PMException {
        Set<Node> all_nodes = getNodes();
        Set<PolicyClassWithActive> pcs = getActivePCs();
        Set<Node> nodes_to_remove = new HashSet<>();

        for (Node node : all_nodes) {
            for (PolicyClassWithActive policyClassWithActive : pcs) {
                if (node.getType() == NodeType.PC) {
                    if (policyClassWithActive.getName().equalsIgnoreCase(node.getName())) {
                        if (!policyClassWithActive.isActive()) {
                            //only remove PC's
                            nodes_to_remove.add(node);
                        }
                    }
                } else {
                    if (node.getProperties().get("namespace") != null) {
                        if (node.getProperties().get("namespace").equalsIgnoreCase(policyClassWithActive.getName())) {
                            //remove nodes UA & OA
                            if (!policyClassWithActive.isActive()) {
                                nodes_to_remove.add(node);
                            }
                        }
                    }
                    if (node.getProperties().get("pc") != null) {
                        if (node.getProperties().get("pc").equalsIgnoreCase(policyClassWithActive.getName())) {
                            //remove nodes pc properties
                            if (!policyClassWithActive.isActive()) {
                                nodes_to_remove.add(node);
                            }
                        }
                    }
                }
            }
        }
        all_nodes.removeAll(nodes_to_remove);
        return all_nodes;
    }

    public Set<String> getPolicies() throws PMException {
        if (superContext != null) {
            return getPDP().getGraphService(superContext).getPolicyClasses();
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public Set<String> getChildren(String name) throws PMException {
        if (superContext != null) {
            return getPDP().getGraphService(superContext).getChildren(name);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public Set<String> getParents(String node) throws PMException {
        if (superContext != null) {
            return getPDP().getGraphService(superContext).getParents(node);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public void assign(String child, String parent) throws PMException {
        if (superContext != null) {
            getPDP().getGraphService(superContext).assign(child, parent);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public void deassign(String child, String parent) throws PMException {
        if (superContext != null) {
            getPDP().getGraphService(superContext).deassign(child, parent);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public void associate(String ua, String target, OperationSet operations) throws PMException {
        if (superContext != null) {
            getPDP().getGraphService(superContext).associate(ua, target, operations);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public void dissociate(String ua, String target) throws PMException {
        if (superContext != null) {
            getPDP().getGraphService(superContext).dissociate(ua, target);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public Map<String, OperationSet> getSourceAssociations(String source) throws PMException {
        if (superContext != null) {
            return getPDP().getGraphService(superContext).getSourceAssociations(source);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public Map<String, OperationSet> getTargetAssociations(String target) throws PMException {
        if (superContext != null) {
            return getPDP().getGraphService(superContext).getTargetAssociations(target);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public Set<Node> search(String name, String type, Map<String, String> properties) throws PMException {
        if (superContext != null) {
            return getPDP().getGraphService(superContext).search(NodeType.toNodeType(type), properties);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public Node getNode(String name) throws PMException {
        if (superContext != null) {
            return getPDP().getGraphService(superContext).getNode(name);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public void fromJson(String s) throws PMException {
        if (superContext != null) {
            getPDP().getGraphService(superContext).fromJson(s);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public String toJson() throws PMException {
        if (superContext != null) {
            return getPDP().getGraphService(superContext).toJson();
        } else {
            throw new PMException("Super Context is Null");
        }
    }
/*    public HashSet<PolicyClassWithActive> getActivePcs () {
        //return the active pcs within the graph
    }*/

    public void processEvent (EventContext eventCtx) throws PMException {
        if (superContext != null) {
            getPDP().getEPP().processEvent(eventCtx);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    // obligation service methods
    public void addObl(InputStream oblString) throws PMException {
        EVRParser evrParser = new EVRParser();
        addObl(evrParser.parse(superContext.getUser(), oblString.toString()));
    }

    public void addObl(Obligation obligation) throws PMException {
        if (superContext != null) {
            getPDP().getObligationsService(superContext).add(obligation, true);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public Obligation getObl(String label) throws PMException {
        if (superContext != null) {
            return getPDP().getObligationsService(superContext).get(label);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public List<Obligation> getAllObls() throws PMException {
        if (superContext != null) {
            return getPDP().getObligationsService(superContext).getAll();
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public void updateObl(String label, Obligation obligation) throws PMException {
        if (superContext != null) {
            getPDP().getObligationsService(superContext).update(label, obligation);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public void deleteObl(String label) throws PMException {
        if (superContext != null) {
            getPDP().getObligationsService(superContext).delete(label);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public void enableObl(String label) throws PMException {
        if (superContext != null) {
            getPDP().getObligationsService(superContext).setEnable(label, true);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public List<Obligation> getEnabledObls() throws PMException {
        if (superContext != null) {
            return getPDP().getObligationsService(superContext).getEnabled();
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


    // prohibition service methods
    public List<Prohibition> getAllProhibitions() throws PMException {
        if (superContext != null) {
            return getPDP().getProhibitionsService(superContext).getAll();
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public void addProhibition(String prohibitionName, String subject, Map<String, Boolean> containers, OperationSet ops, boolean intersection) throws PMException {
        Prohibition.Builder builder = new Prohibition.Builder(prohibitionName, subject, ops)
                .setIntersection(intersection);
        containers.forEach((target, isComplement) -> builder.addContainer(target, isComplement));
        if (superContext != null) {
            getPDP().getProhibitionsService(superContext).add(builder.build());
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public Prohibition getProhibition(String prohibitionName) throws PMException {
        if (superContext != null) {
            return getPDP().getProhibitionsService(superContext).get(prohibitionName);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public List<Prohibition> getProhibitionsFor(String subject) throws PMException {
        if (superContext != null) {
            return getPDP().getProhibitionsService(superContext).getProhibitionsFor(subject);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public List<Prohibition> getProhibitionsFrom(String target) throws PMException {
        if (superContext != null) {
            List<Prohibition> allProhibitions = getPDP().getProhibitionsService(superContext).getAll();
            allProhibitions.removeIf((prohibition) -> !prohibition.getContainers().keySet().contains(target));
            return allProhibitions;
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public void updateProhibition(String prohibitionName, String subject, Map<String, Boolean> containers, OperationSet ops, boolean intersection) throws PMException {
        Prohibition.Builder builder = new Prohibition.Builder(prohibitionName, subject, ops)
                .setIntersection(intersection);
        containers.forEach((target, isComplement) -> builder.addContainer(target, isComplement));
        if (superContext != null) {
            getPDP().getProhibitionsService(superContext).update(prohibitionName, builder.build());
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public void deleteProhibition(String prohibitionName) throws PMException {
        if (superContext != null) {
            getPDP().getProhibitionsService(superContext).delete(prohibitionName);
        } else {
            throw new PMException("Super Context is Null");
        }
    }

//    public void resetProhibitions(UserContext userCtx) throws PMException {
//        if (superContext != null) {
//            g.getProhibitionsService(superContext).reset();
//        } else {
//            throw new PMException("Super Context is Null");
//        }
//    }


    // operation methods
    public Set<String> getAdminOps() throws PMException {
        if (superContext != null) {
            return Operations.ADMIN_OPS;
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public Set<String> getAdminOpsWithStars() throws PMException {
        if (superContext != null) {
            Set<String> ret = getAdminOps();
            ret.add(Operations.ALL_ADMIN_OPS);
            return ret;
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public Set<String> getResourceOps() throws PMException {
        return getPDP().getResourceOps();
    }

    public Set<String> getResourceOpsWithStars() throws PMException {
        if (superContext != null) {
            Set<String> ret = getResourceOps();
            ret.add(Operations.ALL_OPS);
            ret.add(Operations.ALL_RESOURCE_OPS);
            return ret;
        } else {
            throw new PMException("Super Context is Null");
        }
    }

    public Set<String> getAllOps() throws PMException {
        HashSet<String> ops = new HashSet<>();
        ops.addAll(getAdminOps());
        ops.addAll(getResourceOps());
        return ops;
    }

    public Set<String> getAllOpsWithStars() throws PMException {
        HashSet<String> ops = new HashSet<>();
        ops.addAll(getAdminOpsWithStars());
        ops.addAll(getResourceOpsWithStars());
        return ops;
    }

    public void addResourceOps (String... ops) throws PMException {
        getPDP().addResourceOps(ops);
    }

    public void deleteResourceOps (String... ops) throws PMException {
        getPDP().deleteResourceOps(ops);
    }


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
