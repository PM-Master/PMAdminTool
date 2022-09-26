package gov.nist.csd.pm.admintool.graph;

import gov.nist.csd.pm.policy.events.PolicyEvent;
import gov.nist.csd.pm.policy.exceptions.PMException;
import gov.nist.csd.pm.policy.model.access.AccessRightSet;
import gov.nist.csd.pm.policy.model.access.UserContext;
import gov.nist.csd.pm.policy.model.audit.Explain;
import gov.nist.csd.pm.policy.model.graph.nodes.Node;
import gov.nist.csd.pm.policy.model.graph.nodes.NodeType;
import gov.nist.csd.pm.policy.model.graph.relationships.Association;
import gov.nist.csd.pm.policy.model.obligation.Obligation;
import gov.nist.csd.pm.policy.model.prohibition.ContainerCondition;
import gov.nist.csd.pm.policy.model.prohibition.Prohibition;
import gov.nist.csd.pm.policy.model.prohibition.ProhibitionSubject;
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
    private static Set<Node> allPCs;
    private static String superPCId, superUAId, superOAId;
    private static final AccessRightSet RWE = new AccessRightSet("read", "write", "execute");
    private static NGACWSWebClient webClient = new NGACWSWebClient(NGACWSWebClient.LOCALHOST_URL);

    public Boolean getMysql() {
        return webClient.getMySQL();
    }
    public static String getSuperPCId() {return superPCId;}
    public static String getSuperUAId() {return superUAId;}
    public static String getSuperOAId() {return superOAId;}

    /**
     * Gets the singleton instance of this class
     */
    public synchronized static SingletonClient getInstance() {
        rand = new Random();
        if (g == null) {
            try {
                //userContext = new UserContext("super", rand.toString());
                g = new SingletonClient();
                //userContext = new UserContext("super", "1234");
                findSuperConfigurationNodes(g);

                return g;
            } catch (PMException e) {
                e.printStackTrace();
            }
        }
        return g;
    }

    private static void findSuperConfigurationNodes(SingletonClient graph) throws PMException {
        userContext = null;
        for (Node n : graph.getNodes()) {
            //if (n.getProperties().get("namespace") != null && n.getProperties().get("namespace").equals("super")) {
                switch (n.getType()) {
                    case OA:
                        System.out.println("Super OA: " + n.getName());
                        superOAId = n.getName();
                        break;
                    case UA:
                        if (n.getName().equals("super_ua")) {
                            System.out.println("Super UA: " + n.getName());
                            System.out.println(g.checkPermissions(n.getName()));
                            System.out.println(g.getAccessRights(n.getName()));
                            System.out.println(g.checkPermissions(n.getName()));

                            superUAId = n.getName();
                        }
                        break;
                    case U:
                        if (n.getName().equals("super")) {
                            System.out.println("Super U: " + n.getName());
                            userContext = new UserContext(n.getName(), "1234");
                        }
                        //webClient.getUserCtx(userContext.getUser());
                        break;
                    case PC:
                        System.out.println("Super PC: " + n.getName());
                        superPCId = n.getName();
                        break;
                }
            //}
        }
        graph.setResourceAccessRights(RWE);
    }

    public void setUserContext(String username) {
        userContext = new UserContext(username, rand.toString());
    }

    public String getCurrentContext() {
        if (userContext != null)
            return userContext.getUser();
        else
            return "No User Context";
    }

    public static Set<Node> getAllPCs() {
        allPCs = webClient.getAllPCs();
        return allPCs;
    }

    public static void resetAllPCs() {
        webClient.resetAllPCs();
        allPCs = webClient.getAllPCs();
    }

    public String getPCNames() {
        List<String> pcs = new ArrayList<>();
        for (Node pc: allPCs) {
            pcs.add(pc.getName());
        }
        return "All pcs: " + pcs;
    }

    public void createPolicyClass(String name, Map<String, String> properties) throws PMException {
        String newPC_name = webClient.createPolicyClass(name, properties);
        Node newPC = g.getNode(newPC_name);
        allPCs.add(newPC);
    }

    // graph service methods
    public void reset() throws PMException{
        webClient.reset();
    }

    public void createNode(String name, NodeType type, Map<String, String> properties, String parent) throws PMException {
        webClient.createNode(new Node(name, type, properties), parent);
    }

    public String getPolicyClassDefault(String pc, NodeType type) throws PMException {
        return webClient.getPolicyClassDefault(pc, type);
    }

    public void updateNode(String name, Map<String, String> properties) throws PMException {
        Node node = new Node();
        node.setName(name);
        node.setProperties(properties);
        webClient.updateNode(name,node);
    }

    public boolean exists(String name) throws PMException {
        return webClient.exists(name);
    }

    public void deleteNode(String name) throws PMException {
        webClient.deleteNode(name);
    }

    public List<Node> getNodes() throws PMException {
        return webClient.getNodes();
    }

    public List<String> getPolicies() throws PMException {
        return webClient.getPolicies();
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
    }

    public Set<String> getChildrenNoSuperPolicy(String name) throws PMException {
        List<String> children = webClient.getChildrenNoSuperPolicy(name);
        return new HashSet<>(children);
    }

    public Set<String> getParents(String node) throws PMException {
        String parents  = webClient.getParents(node).iterator().next();
        return stringToSet(parents);
    }

    public void assign(String child, String parent) throws PMException {
        webClient.assign(child, parent);
    }

    public void deassign(String child, String parent) throws PMException {
        webClient.deassign(child, parent);
    }

    public void associate(String ua, String target, AccessRightSet operations) throws PMException {
        webClient.associate(ua, target, operations);
    }

    public void dissociate(String ua, String target) throws PMException {
        webClient.dissociate(ua, target);
    }

    public List<Association> getSourceAssociations(String source) throws PMException {
        System.out.println("source: " + source);
        System.out.println("Association: " + webClient.getSourceAssociations(source));

        return webClient.getSourceAssociations(source);
    }

    public List<Association> getTargetAssociations(String target) throws PMException {
        return webClient.getTargetAssociations(target);
    }

    public Node getNode(String name) throws PMException {
        return webClient.getNode(name);
    }

    public void processEvent (PolicyEvent eventCtx) throws PMException {
        webClient.processEvent(eventCtx);
    }

    // obligation service methods
    /*public Obligation parseObligationYaml (String oblString) throws PMException {
        EVRParser parser = new EVRParser();
        return parser.parse(userContext.getUser(), oblString);
    }*/

    public void fromPAL(String s) throws PMException {
        webClient.fromPal(s);
    }

    public String toPal() throws PMException {
        return webClient.toPal();
    }

    public void addObl(Obligation obligation) throws PMException {
        webClient.addObl(obligation);
    }

    public List<Obligation> getAllObls() throws PMException {
        return webClient.getAllObls();
    }

    public void updateObl(String label, Obligation obligation) throws PMException {
        webClient.updateObl(label,obligation);
    }

    public void deleteObl(String label) throws PMException {
        webClient.deleteObl(label);
    }

    // prohibition service methods
    public List<Prohibition> getAllProhibitions() throws PMException {
        return webClient.getAllProhibitions();
    }

    public void addProhibition(String prohibitionName, ProhibitionSubject subject, Map<String, Boolean> containers, AccessRightSet ops, boolean intersection) throws PMException {
        List<ContainerCondition> containerConditions = new ArrayList<>(containers.size());

        for (String c: containers.keySet()) {
            containerConditions.add(new ContainerCondition(c, containers.get(c)));
        }

        //Prohibition prohibition = new Prohibition(prohibitionName, subject, ops, intersection, containerConditions);

        NGACWSWebClient.ProhibitionInfo prohibitionInfo = new NGACWSWebClient.ProhibitionInfo();
        prohibitionInfo.containers = containers;
        prohibitionInfo.ops = ops;
        webClient.addProhibition(prohibitionName,subject,prohibitionInfo,intersection);
    }

    public List<Prohibition> getProhibitionsFor(String subject) throws PMException {
        return webClient.getProhibitionsFor(subject);
    }

    public void updateProhibition(String prohibitionName, ProhibitionSubject subject, Map<String, Boolean> containers, AccessRightSet ops, boolean intersection) throws PMException {
        List<ContainerCondition> containerConditions = new ArrayList<>(containers.size());

        for (String c: containers.keySet()) {
            containerConditions.add(new ContainerCondition(c, containers.get(c)));
        }

        Prohibition prohibition = new Prohibition(prohibitionName, subject, ops, intersection, containerConditions);

        NGACWSWebClient.ProhibitionInfo prohibitionInfo = new NGACWSWebClient.ProhibitionInfo();
        prohibitionInfo.containers = containers;
        prohibitionInfo.ops = ops;
        webClient.updateProhibition(prohibitionName,subject,prohibitionInfo,intersection);
    }


    public void deleteProhibition(String prohibitionName) throws PMException {
        webClient.deleteProhibition(prohibitionName);
    }

    // operation methods
    public Set<String> getAdminOps() throws PMException {
        String ops = webClient.getAdminOps().iterator().next();
        return stringToSet(ops);
    }

    public Set<String> getAdminOpsWithStars() throws PMException {
        String ops = webClient.getAdminOpsWithStars().iterator().next();
        return stringToSet(ops);
    }

    public Set<String> getResourceOps() throws PMException {
        String ops = webClient.getResourceOps().iterator().next();
        return stringToSet(ops);
    }

    public Set<String> getResourceOpsWithStars() throws PMException {
        String ops = webClient.getResourceOpsWithStars().iterator().next();
        return stringToSet(ops);
    }

    public Set<String> getAllOpsWithStars() throws PMException {
        Set<String> ops = new HashSet<>();
        ops.addAll(getAdminOpsWithStars());
        ops.addAll(getResourceOpsWithStars());
        return ops;
    }

    public void addResourceOps (String... ops) throws PMException {
        System.out.println("Ops to add: " + ops);
        webClient.addResourceOps(ops);
    }

    public void deleteResourceOps (String... ops) throws PMException {
        webClient.deleteResourceOps(ops);
    }

    public Explain explain(UserContext userContext, String target) {
        return webClient.explain(userContext, target);
    }
    public String getExplanation(String target) {
        return webClient.getExplanation(target);
    }

    public boolean checkPermissions (String target, String... ops) throws PMException {
        return webClient.checkPermissions(target, ops);
    }

    public AccessRightSet getResourceAccessRights() throws PMException {
        return webClient.getResourceAccessRights();
    }

    public void setResourceAccessRights(AccessRightSet accessRightSet) throws PMException {
        webClient.setResourceAccessRights(accessRightSet);
    }

    public AccessRightSet getAccessRights(String target) throws PMException {
        return webClient.getAccessRights(target);
    }
}
