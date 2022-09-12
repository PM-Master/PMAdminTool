package gov.nist.csd.pm.admintool.graph;

import gov.nist.csd.pm.policy.events.PolicyEvent;
import gov.nist.csd.pm.policy.exceptions.PMException;
import gov.nist.csd.pm.policy.model.access.AccessRightSet;
import gov.nist.csd.pm.policy.model.access.UserContext;
import gov.nist.csd.pm.policy.model.audit.Explain;
import gov.nist.csd.pm.policy.model.graph.nodes.Node;
import gov.nist.csd.pm.policy.model.graph.nodes.NodeType;
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

    private static NGACWSWebClient webClient = new NGACWSWebClient(NGACWSWebClient.LOCALHOST_URL);

    public SingletonClient() throws PMException {
    }

    public Boolean getMysql() {
        return webClient.getMySQL();
    }


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

    public Node createPolicyClass(String name, Map<String, String> properties) throws PMException {
        Node newPC = webClient.createPolicyClass(name, properties);
        allPCs.add(newPC);
        return newPC;
    }

    // graph service methods
    public void reset() throws PMException{
        webClient.reset();
    }

    public Node createNode(String name, NodeType type, Map<String, String> properties, String parent) throws PMException {
        return webClient.createNode(new Node(name, type, properties), parent);
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

    public void deleteNode(String name) throws PMException {
        webClient.deleteNode(name);
    }

    public Set<Node> getNodes() throws PMException {
        return webClient.getNodes();
    }

    public Set<String> getPolicies() throws PMException {
        String pcs  = webClient.getPolicies().iterator().next();
        return stringToSet(pcs);
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
        String children = webClient.getChildrenNoSuperPolicy(name).iterator().next();
        return stringToSet(children);
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

    public Map<String, AccessRightSet> getSourceAssociations(String source) throws PMException {
        return webClient.getSourceAssociations(source);
    }

    public Map<String, AccessRightSet> getTargetAssociations(String target) throws PMException {
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

        Prohibition prohibition = new Prohibition(prohibitionName, subject, ops, intersection, containerConditions);

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
        HashSet<String> ops = new HashSet<>();
        ops.addAll(getAdminOpsWithStars());
        ops.addAll(getResourceOpsWithStars());
        return ops;
    }

    public void addResourceOps (String... ops) throws PMException {
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
}
