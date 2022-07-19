package gov.nist.csd.pm.admintool.graph;

import gov.nist.csd.pm.epp.events.EventContext;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.operations.OperationSet;
import gov.nist.csd.pm.pdp.audit.model.Explain;
import gov.nist.csd.pm.pdp.services.UserContext;
import gov.nist.csd.pm.pip.graph.model.nodes.Node;
import gov.nist.csd.pm.pip.graph.model.nodes.NodeType;
import gov.nist.csd.pm.pip.obligations.evr.EVRParser;
import gov.nist.csd.pm.pip.obligations.model.Obligation;
import gov.nist.csd.pm.pip.prohibitions.model.Prohibition;
import gov.nist.ngacclient.api.NGACWSWebClient;

import java.util.*;

/**
 * The "In-Memory" graph used throughout the entirety of the admin tool.
 *
 * There is only one instance which can be retrieved using SingletonClient.getInstance().
 *
 * UserContext is used to track the current user of the application. At the start of the application it is the
 * super user, but if the setUserContext() method is used it will change from the super context.
 *
 */
public class SingletonClient {
    private static SingletonClient g; // the single instance
    private static UserContext userContext;
    private static Random rand;
    private static Set<PolicyClassWithActive> activePCs;
//    private static MySQLConnection connection = new MySQLConnection();

    private static NGACWSWebClient webClient = new NGACWSWebClient(NGACWSWebClient.LOCALHOST_URL);

    public SingletonClient() throws PMException {
    }

//    public synchronized static PDP getPDP() {
//        return getInstance().pdp;
//    }

    public Boolean getMysql() {
        return webClient.getMySQL();
    }

//    public void setMysql(Boolean mysql) {
//        isMysql = mysql;
//    }

    /**
     * Gets the singleton instance of this class
     */
    public synchronized static SingletonClient getInstance() {
        rand = new Random();
        if (g == null) {
            try {
                return new SingletonClient();
            } catch (PMException e) {
                e.printStackTrace();
            }
        }
        return g;
    }

//    private synchronized static void fixGraphData(Graph graph) {
//        try {
//            graph = new MemDBGraph(graph);
//
//            Prohibitions prohibitions = new MySQLProhibitions(connection);
//            prohibitions = new MemDBProhibitions(prohibitions);
//
//            PAP pap = new PAP(
//                    new GraphAdmin(graph),
//                    new ProhibitionsAdmin(prohibitions),
//                    new ObligationsAdmin(new MemObligations())
//            );
//
//            g = new SingletonClient();
//
//            PDP pdp = newPDP(
//                    pap,
//                    new EPPOptions(new RecordFunctionExecutor()),
//                    new OperationSet(Operations.READ, Operations.WRITE, Operations.OBJECT_ACCESS));
//
//            g.pdp = pdp;
//
//
//            System.out.println("MySQLGraph");
//            findSuperConfigurationNodes(graph);
//            findActivePCS(graph);
//        } catch (PMException e) {
//            System.out.println(e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    private synchronized static void fixGraphDataMem(Graph graph) {
//        try {
//            /// Creating new PAP and PDP
//            PAP pap = new PAP(
//                    new GraphAdmin(graph),
//                    new ProhibitionsAdmin(new MemProhibitions()),
//                    new ObligationsAdmin(new MemObligations())
//            );
//
//            g = new SingletonClient();
//
//            PDP pdp = newPDP(
//                    pap,
//                    new EPPOptions(new RecordFunctionExecutor()),
//                    new OperationSet(Operations.READ, Operations.WRITE, Operations.OBJECT_ACCESS)
//            );
//
//            g.pdp = pdp;
//
//            System.out.println("MemGraph");
//            findSuperConfigurationNodes(graph);
//            findActivePCS(graph);
//        } catch (PMException e) {
//            System.out.println(e.getMessage());
//            e.printStackTrace();
//        }
//    }

    public void setUserContext(String username) {
        userContext = new UserContext(username, rand.toString());
    }

    public String getCurrentContext() {
        if (userContext != null)
            return userContext.getUser();
        else
            return "No User Context";
    }

//    private static void findActivePCS(Graph graph) throws PMException{
//        activePCs = new HashSet<>();
//        for (Node n : graph.getNodes()) {
//            if (n.getType().equals(NodeType.PC)) {
//                activePCs.add(new PolicyClassWithActive(n));
//            }
//        }
//    }

//    private static void findSuperConfigurationNodes(Graph graph) throws PMException {
//        userContext = null;
//        for (Node n : graph.getNodes()) {
//            if (n.getProperties().get("namespace") != null && n.getProperties().get("namespace").equals("super")) {
//                switch (n.getType()) {
//                    case OA:
//                        System.out.println("Super OA: " + n.getName());
//                        superOAId = n.getName();
//                        break;
//                    case UA:
//                        if (n.getName().equals("super_ua2")) {
//                            System.out.println("Super UA: " + n.getName());
//                            superUAId = n.getName();
//                        }
//                        break;
//                    case U:
//                        System.out.println("Super U: " + n.getName());
//                        userContext = new UserContext(n.getName(), rand.toString());
//                        break;
//                    case PC:
//                        System.out.println("Super PC: " + n.getName());
//                        superPCId = n.getName();
//                        break;
//                }
//            }
//
//
//        }
//    }

//    private static UserContext getUserContext() {
//        return userContext;
//    }
//
//    public static String getSuperPCId() {
//        return superPCId;
//    }
//
//    public static String getSuperOAId() {
//        return superOAId;
//    }
//
//    public static String getSuperUAId() {
//        return superUAId;
//    }

    public static Set<PolicyClassWithActive> getActivePCs() {
        Set<PolicyClassWithActive> activePCs = new HashSet<>();
        Set<Node> activeNodes = webClient.getActivePCs();
        for (Node activeNode: activeNodes)
        {
            activePCs.add(new PolicyClassWithActive(activeNode));
        }
        return activePCs;
    }

    public boolean isPCActive(Node pc) {

        activePCs = this.getActivePCs();
        for (PolicyClassWithActive pcsa : activePCs) {
            if (pcsa.pc.equals(pc)) {
                return pcsa.isActive();
            }
        }

        return false;
    }

    public static void resetActivePCs() {
        webClient.resetActivePCs();
//        activePCs.removeIf(policyClassWithActive -> !policyClassWithActive.getName().equals("super_pc"));
    }


    public String toString() {
        List<String> pcs = new ArrayList<>();
        for (PolicyClassWithActive pc: activePCs) {
            if (pc.isActive()){
                pcs.add(pc.name);
            }
        }
        return "Active pcs: " + pcs;
    }

    public Node createPolicyClass(String name, Map<String, String> properties) throws PMException {
        Node newPC = webClient.createPolicyClass(name, properties);
        activePCs.add(new PolicyClassWithActive(newPC));
//        if (userContext != null) {
//            Node newPC = getPDP().getGraphService(userContext).createPolicyClass(name, properties);
//            activePCs.add(new PolicyClassWithActive(newPC));
//            return newPC;
//        } else {
//            throw new PMException("User Conext is Null");
//        }
        return newPC;
    }

    public void updateGraph (boolean isMySQL){
        webClient.updateGraph(isMySQL);
//        g = null;
//        if (isMySQL) {
//            fixGraphData(new MySQLGraph(connection));
//            g.setMysql(true);
//        } else {
//            fixGraphDataMem(new MemGraph());
//            g.setMysql(false);
//        }
//        return g;
    }

    // graph service methods
    public void reset() throws PMException{
        webClient.reset();
//        getPDP().getGraphService(userContext).reset(userContext);
//        dacConfigured = false;
    }

    public Node createNode(String name, NodeType type, Map<String, String> properties, String parent) throws PMException {
        return webClient.createNode(new Node(name, type, properties), parent);
//        if (userContext != null) {
//            return getPDP().getGraphService(userContext).createNode(name, type, properties, parent );
//        } else {
//            throw new PMException("User Context is Null");
//        }
    }

    public String getPolicyClassDefault(String pc, NodeType type) throws PMException {
        return webClient.getPolicyClassDefault(pc, type);
//        if (pc.equalsIgnoreCase("0")) {
//            if (userContext != null) {
//                return getPDP().getGraphService(userContext).getPolicyClassDefault(superPCId, type);
//            } else {
//                throw new PMException("User Context is Null");
//            }
//        } else return getPDP().getGraphService(userContext).getPolicyClassDefault(pc, type);
    }

    public void updateNode(String name, Map<String, String> properties) throws PMException {
        Node node = new Node();
        node.setName(name);
        node.setProperties(properties);
        webClient.updateNode(name,node);
//        if (userContext != null) {
//            getPDP().getGraphService(userContext).updateNode(name, properties);
//        } else {
//            throw new PMException("User Context is Null");
//        }
    }

    public void deleteNode(String name) throws PMException {
        webClient.deleteNode(name);
//        if (userContext != null) {
//            getPDP().getGraphService(userContext).deleteNode(name);
//        } else {
//            throw new PMException("User Context is Null");
//        }
    }

//    public boolean exists(String name) throws PMException {
//        if (userContext != null) {
//            return getPDP().getGraphService(userContext).exists(name);
//        } else {
//            throw new PMException("User Context is Null");
//        }
//    }

    public Set<Node> getNodes() throws PMException {
        return webClient.getNodes();

//        if (userContext != null) {
//            return getPDP().getGraphService(userContext).getNodes();
//        } else {
//            throw new PMException("User Context is Null");
//        }
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
        String pcs  = webClient.getPolicies().iterator().next();
        return stringToSet(pcs);
//        if (userContext != null) {
//            return getPDP().getGraphService(userContext).getPolicyClasses();
//        } else {
//            throw new PMException("User Context is Null");
//        }
    }

    // Utility function

    private Set<String> stringToSet(String str) {
        Set<String> set = new HashSet<>();
        str = str
                .replace("[","")
                .replace("]","")
                .replaceAll("\"","");
        if (str.isEmpty()) return set;
        set =  new HashSet<String>(Arrays.asList(str.split(",")));
        return set;
    }
    public Set<String> getChildren(String name) throws PMException {
        String children  = webClient.getChildren(name).iterator().next();
        return stringToSet(children);
//        if (userContext != null) {
//            return getPDP().getGraphService(userContext).getChildren(name);
//        } else {
//            throw new PMException("User Context is Null");
//        }
    }

    public Set<String> getChildrenNoSuperPolicy(String name) throws PMException {
        String children = webClient.getChildrenNoSuperPolicy(name).iterator().next();
        return stringToSet(children);
//        if (userContext != null) {
//            Set<String> children_noSuperPolicy = getPDP().getGraphService(userContext).getChildren(name);
//            Collection<String> superPolicyNames = new ArrayList<>();
//            superPolicyNames.add("super_ua1");
//            superPolicyNames.add("super_ua2");
//            children_noSuperPolicy.removeAll(superPolicyNames);
//            return children_noSuperPolicy;
//        } else {
//            throw new PMException("User Context is Null");
//        }
    }

    public Set<String> getParents(String node) throws PMException {
        String parents  = webClient.getParents(node).iterator().next();
        return stringToSet(parents);
//        if (userContext != null) {
//            return getPDP().getGraphService(userContext).getParents(node);
//        } else {
//            throw new PMException("User Context is Null");
//        }
    }

    public void assign(String child, String parent) throws PMException {
        webClient.assign(child, parent);
//        if (userContext != null) {
//            getPDP().getGraphService(userContext).assign(child, parent);
//        } else {
//            throw new PMException("User Context is Null");
//        }
    }

    public void deassign(String child, String parent) throws PMException {
        webClient.deassign(child, parent);
//        if (userContext != null) {
//            getPDP().getGraphService(userContext).deassign(child, parent);
//        } else {
//            throw new PMException("User Context is Null");
//        }
    }

    public void associate(String ua, String target, OperationSet operations) throws PMException {
        webClient.associate(ua, target, operations);
//        if (userContext != null) {
//            getPDP().getGraphService(userContext).associate(ua, target, operations);
//        } else {
//            throw new PMException("User Context is Null");
//        }
    }

    public void dissociate(String ua, String target) throws PMException {
        webClient.dissociate(ua, target);
//        if (userContext != null) {
//            getPDP().getGraphService(userContext).dissociate(ua, target);
//        } else {
//            throw new PMException("User Context is Null");
//        }
    }

    public Map<String, OperationSet> getSourceAssociations(String source) throws PMException {
        return webClient.getSourceAssociations(source);
//        if (userContext != null) {
//            return getPDP().getGraphService(userContext).getSourceAssociations(source);
//        } else {
//            throw new PMException("User Context is Null");
//        }
    }

    public Map<String, OperationSet> getTargetAssociations(String target) throws PMException {
        return webClient.getTargetAssociations(target);
//        if (userContext != null) {
//            return getPDP().getGraphService(userContext).getTargetAssociations(target);
//        } else {
//            throw new PMException("User Context is Null");
//        }
    }

//    public Set<Node> search(String name, String type, Map<String, String> properties) throws PMException {
//        if (userContext != null) {
//            return getPDP().getGraphService(userContext).search(NodeType.toNodeType(type), properties);
//        } else {
//            throw new PMException("User Context is Null");
//        }
//    }

    public Node getNode(String name) throws PMException {
        return webClient.getNode(name);

//        if (userContext != null) {
//            return getPDP().getGraphService(userContext).getNode(name);
//        } else {
//            throw new PMException("User Context is Null");
//        }
    }

    public void fromJson(String s) throws PMException {
        webClient.fromJson(s);
//        if (userContext != null) {
//            getPDP().getGraphService(userContext).fromJson(s);
//        } else {
//            throw new PMException("User Context is Null");
//        }
    }

    public String toJson() throws PMException {
        return webClient.toJson();
//        if (userContext != null) {
//            return getPDP().getGraphService(userContext).toJson();
//        } else {
//            throw new PMException("User Context is Null");
//        }
    }
/*    public HashSet<PolicyClassWithActive> getActivePcs () {
        //return the active pcs within the graph
    }*/

    public void processEvent (EventContext eventCtx) throws PMException {
        webClient.processEvent(eventCtx);
//        if (userContext != null) {
//            getPDP().getEPP().processEvent(eventCtx);
//        } else {
//            throw new PMException("User Context is Null");
//        }
    }

    // obligation service methods
    public Obligation parseObligationYaml (String oblString) throws PMException {
        EVRParser parser = new EVRParser();
        return parser.parse(userContext.getUser(), oblString);
    }

//    public void addObl(String oblString) throws PMException {
//        addObl(parseObligationYaml(oblString));
//    }

    public void addObl(Obligation obligation) throws PMException {
        webClient.addObl(obligation);
//        if (userContext != null) {
//            getPDP().getObligationsService(userContext).add(obligation, true);
//        } else {
//            throw new PMException("User Context is Null");
//        }
    }

//    public Obligation getObl(String label) throws PMException {
//        return webClient.getObl(label);
////        if (userContext != null) {
////            return getPDP().getObligationsService(userContext).get(label);
////        } else {
////            throw new PMException("User Context is Null");
////        }
//    }

    public List<Obligation> getAllObls() throws PMException {
        return webClient.getAllObls();
//        if (userContext != null) {
//            return getPDP().getObligationsService(userContext).getAll();
//        } else {
//            throw new PMException("User Context is Null");
//        }
    }

    public void updateObl(String label, Obligation obligation) throws PMException {
        webClient.updateObl(label,obligation);
//        if (userContext != null) {
//            getPDP().getObligationsService(userContext).update(label, obligation);
//        } else {
//            throw new PMException("User Context is Null");
//        }
    }

    public void deleteObl(String label) throws PMException {
        webClient.deleteObl(label);
//        if (userContext != null) {
//            getPDP().getObligationsService(userContext).delete(label);
//        } else {
//            throw new PMException("User Context is Null");
//        }
    }

//    public void enableObl(String label) throws PMException {
//        webClient.enableObl(label);
////        if (userContext != null) {
////            getPDP().getObligationsService(userContext).setEnable(label, true);
////        } else {
////            throw new PMException("User Context is Null");
////        }
//    }

//    public List<Obligation> getEnabledObls() throws PMException {
//        return webClient.getEnabledObls();
////        if (userContext != null) {
////            return getPDP().getObligationsService(userContext).getEnabled();
////        } else {
////            throw new PMException("User Context is Null");
////        }
//    }

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
        return webClient.getAllProhibitions();
//        if (userContext != null) {
//            return getPDP().getProhibitionsService(userContext).getAll();
//        } else {
//            throw new PMException("User Context is Null");
//        }
    }

    public void addProhibition(String prohibitionName, String subject, Map<String, Boolean> containers, OperationSet ops, boolean intersection) throws PMException {
        Prohibition.Builder builder = new Prohibition.Builder(prohibitionName, subject, ops)
                .setIntersection(intersection);
        containers.forEach((target, isComplement) -> builder.addContainer(target, isComplement));
        NGACWSWebClient.ProhibitionInfo prohibitionInfo = new NGACWSWebClient.ProhibitionInfo();
        prohibitionInfo.containers = containers;
        prohibitionInfo.ops = ops;
        webClient.addProhibition(prohibitionName,subject,prohibitionInfo,intersection);
//        if (userContext != null) {
//            getPDP().getProhibitionsService(userContext).add(builder.build());
//        } else {
//            throw new PMException("User Context is Null");
//        }
    }

//    public Prohibition getProhibition(String prohibitionName) throws PMException {
//        return webClient.getProhibition(prohibitionName);
////        if (userContext != null) {
////            return getPDP().getProhibitionsService(userContext).get(prohibitionName);
////        } else {
////            throw new PMException("User Context is Null");
////        }
//    }

    public List<Prohibition> getProhibitionsFor(String subject) throws PMException {
        return webClient.getProhibitionsFor(subject);
//        if (userContext != null) {
//            return getPDP().getProhibitionsService(userContext).getProhibitionsFor(subject);
//        } else {
//            throw new PMException("User Context is Null");
//        }
    }

//    public List<Prohibition> getProhibitionsFrom(String target) throws PMException {
//        return webClient.getProhibitionsFrom(target);
////        if (userContext != null) {
////            List<Prohibition> allProhibitions = getPDP().getProhibitionsService(userContext).getAll();
////            allProhibitions.removeIf((prohibition) -> !prohibition.getContainers().keySet().contains(target));
////            return allProhibitions;
////        } else {
////            throw new PMException("User Context is Null");
////        }
//    }

    public void updateProhibition(String prohibitionName, String subject, Map<String, Boolean> containers, OperationSet ops, boolean intersection) throws PMException {
        Prohibition.Builder builder = new Prohibition.Builder(prohibitionName, subject, ops)
                .setIntersection(intersection);
        containers.forEach((target, isComplement) -> builder.addContainer(target, isComplement));
        NGACWSWebClient.ProhibitionInfo prohibitionInfo = new NGACWSWebClient.ProhibitionInfo();
        prohibitionInfo.containers = containers;
        prohibitionInfo.ops = ops;
        webClient.updateProhibition(prohibitionName,subject,prohibitionInfo,intersection);
//        if (userContext != null) {
//            getPDP().getProhibitionsService(userContext).update(prohibitionName, builder.build());
//        } else {
//            throw new PMException("User Context is Null");
//        }
    }


    public void deleteProhibition(String prohibitionName) throws PMException {
        webClient.deleteProhibition(prohibitionName);
//        if (userContext != null) {
//            getPDP().getProhibitionsService(userContext).delete(prohibitionName);
//        } else {
//            throw new PMException("User Context is Null");
//        }
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
        String ops = webClient.getAdminOps().iterator().next();
        return stringToSet(ops);
//        if (userContext != null) {
//            return Operations.ADMIN_OPS;
//        } else {
//            throw new PMException("User Context is Null");
//        }
    }

    public Set<String> getAdminOpsWithStars() throws PMException {
        String ops = webClient.getAdminOpsWithStars().iterator().next();
        return stringToSet(ops);
//        if (userContext != null) {
//            Set<String> ret = getAdminOps();
//            ret.add(Operations.ALL_ADMIN_OPS);
//            return ret;
//        } else {
//            throw new PMException("User Context is Null");
//        }
    }

    public Set<String> getResourceOps() throws PMException {
        String ops = webClient.getResourceOps().iterator().next();
        return stringToSet(ops);
//        return getPDP().getResourceOps();
    }

    public Set<String> getResourceOpsWithStars() throws PMException {
        String ops = webClient.getResourceOpsWithStars().iterator().next();
        return stringToSet(ops);
//        if (userContext != null) {
//            Set<String> ret = getResourceOps();
//            ret.add(Operations.ALL_OPS);
//            ret.add(Operations.ALL_RESOURCE_OPS);
//            return ret;
//        } else {
//            throw new PMException("User Context is Null");
//        }
    }

//    public Set<String> getAllOps() throws PMException {
//        HashSet<String> ops = new HashSet<>();
//        ops.addAll(getAdminOps());
//        ops.addAll(getResourceOps());
//        return ops;
//    }

    public Set<String> getAllOpsWithStars() throws PMException {
        HashSet<String> ops = new HashSet<>();
        ops.addAll(getAdminOpsWithStars());
        ops.addAll(getResourceOpsWithStars());
        return ops;
    }

    public void addResourceOps (String... ops) throws PMException {
        webClient.addResourceOps(ops);
//        getPDP().addResourceOps(ops);
    }

    public void deleteResourceOps (String... ops) throws PMException {
        webClient.deleteResourceOps(ops);
//        getPDP().deleteResourceOps(ops);
    }

    public Explain explain(UserContext userContext, String target) {
        return webClient.explain(userContext, target);
    }
    public String getExplanation(String target) {
        return webClient.getExplanation(target);
    }
//        AnalyticsService analyticsService = getPDP().getAnalyticsService(userContext);
//        String explanation;
//        Explain explain = null;
//
//        try {
//            explain = analyticsService.explain(userContext.getUser(), target);
//        } catch (PMException e) {
//            MainView.notify(e.getMessage(), MainView.NotificationType.ERROR);
//            e.printStackTrace();
//        }
//
//        if (explain != null) {
//            String ret = "";
//            // Explain returns two things:
//            //  1. The permissions the user has on the target
//            //  2. A breakdown of permissions per policy class and paths in each policy class
//            ret +=  "'" + userContext.getUser() + "' has the following permissions on the target '" + target + "': \n";
//            Set<String> permissions = explain.getPermissions();
//            for (String perm: permissions) {
//                ret += "\t- " + perm + "\n";
//            }
//            ret += "\n";
//
//
//            // policyClasses maps the name of a policy class node to a Policy Class object
//            // a policy class object contains the permissions the user has on the target node
//            //   in that policy class
//            ret += "The following section shows a more detailed permission breakdown from the perspective of each policy class:\n";
//            Map<String, PolicyClass> policyClasses = explain.getPolicyClasses();
//            int i = 1;
//            for (String pcName : policyClasses.keySet()) {
//                ret += "\t" + i + ". '" + pcName + "':\n";
//                PolicyClass policyClass = policyClasses.get(pcName);
//
//                // the operations available to the user on the target under this policy class
//                Set<String> operations = policyClass.getOperations();
//                ret += "\t\t- Permissions (Given by this PC):\n";
//                for (String op: operations) {
//                    ret += "\t\t\t- " + op + "\n";
//                }
//                // the paths from the user to the target
//                // A Path object contains the path and the permissions the path provides
//                // the path is just a list of nodes starting at the user and ending at the target node
//                // example: u1 -> ua1 -> oa1 -> o1 [read]
//                //   the association ua1 -> oa1 has the permission [read]
//                ret += "\t\t- Paths (How each permission is found):\n";
//                Set<Path> paths = policyClass.getPaths();
//                for (Path path : paths) {
//                    ret += "\t\t\t";
//                    // this is just a list of nodes -> [u1, ua1, oa1, o1]
//                    List<Node> nodes = path.getNodes();
//                    for (Node n: nodes) {
//                        ret += "'" + n.getName() + "'";
//                        if (!nodes.get(nodes.size()-1).equals(n)) { // not final node
//                            ret += " > ";
//                        }
//                    }
//
//                    // this is the operations in the association between ua1 and oa1
//                    Set<String> pathOps = path.getOperations();
//                    ret += ":\n\t\t\t\t" + pathOps;
//                    // This is the string representation of the path (i.e. "u1-ua1-oa1-o1 ops=[r, w]")
//                    String pathString = path.toString();
//                    ret += "\n";
//                }
//                i++;
//            }
//
//            explanation = ret;
//        } else {
//            explanation = "Returned Audit was null";
//        }
//        return explanation;
//    }

    public boolean checkPermissions (String target, String... ops) throws PMException {
        return webClient.checkPermissions(target, ops);
//        Set<String> permissions = getPDP().getAnalyticsService(userContext).getPermissions(target);
//        return Stream.of(ops).allMatch(op -> permissions.contains(op));
    }

    // policies methods
//    public void configureDAC(String DACname) throws PMException {
//        DAC.configure(DACname, pdp, userContext);
//        findActivePCS(pdp.getGraphService(userContext));
//        dacConfigured = true;
//    }

//    public void delegate(String delegatorName, String delegateeName,
//                         OperationSet ops, Set<String> targetNames) throws PMException {
//        UserContext delegatorContext = new UserContext(delegatorName);
//        DAC.delegate(pdp, delegatorContext, delegateeName, ops, targetNames);
//    }


    // Policy class with active feild class
    public static class PolicyClassWithActive {
        Node pc; // reference to actual pc node
        String name;
        boolean active;

        public PolicyClassWithActive(Node pc) {
            this.pc = pc;
            name = pc.getName();
            this.active = true;
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
