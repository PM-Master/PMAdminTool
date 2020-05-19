package gov.nist.csd.pm.admintool.actions.tests;

import gov.nist.csd.pm.admintool.graph.SingletonGraph;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.pdp.audit.model.Explain;
import gov.nist.csd.pm.pdp.audit.model.Path;
import gov.nist.csd.pm.pdp.audit.model.PolicyClass;
import gov.nist.csd.pm.pdp.services.UserContext;
import gov.nist.csd.pm.pip.graph.model.nodes.Node;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class CheckPermission extends Test {
    private static Random rand;

    public CheckPermission() {
        setName("checkPermission");
        addParam("uID", Type.NODETYPE);
        addParam("targetID", Type.NODETYPE);
        addParam("permission", Type.OPERATION);
    }

    @Override
    public boolean run() {
        Node uID = ((Node)getParams().get("uID").getValue());
        Node targetID = ((Node)getParams().get("targetID").getValue());
        String permission = (String)getParams().get("permission").getValue();

        if (uID != null && targetID != null && permission != null) {
            SingletonGraph g = SingletonGraph.getInstance();
            try {
                Set<String> perms = g.getAnalyticsService(new UserContext(uID.getName(), rand.toString())).getPermissions(targetID.getName());
                for (String perm: perms) {
                    if (perm.equals(permission)) {
                        return true;
                    }
                }
            } catch (PMException e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
            }
        }
        return false;
    }

    @Override
    public String explain() {
        Node user = ((Node)getParams().get("uID").getValue());
        Node target = ((Node)getParams().get("targetID").getValue());

        if (user != null && target != null) {
            SingletonGraph g = SingletonGraph.getInstance();
            Explain explain = null;

            try {
                explain = g.getAnalyticsService(new UserContext(user.getName(), rand.toString())).explain(user.getName(), target.getName());
            } catch (PMException e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
            }

            if (explain != null) {
                String ret = "";
                // Explain returns two things:
                //  1. The permissions the user has on the target
                //  2. A breakdown of permissions per policy class and paths in each policy class
                ret +=  "'" + user.getName() + "' has the following permissions on the target '" + target.getName() + "': \n";
                Set<String> permissions = explain.getPermissions();
                for (String perm: permissions) {
                    ret += "\t- " + perm + "\n";
                }
                ret += "\n";


                // policyClasses maps the name of a policy class node to a Policy Class object
                // a policy class object contains the permissions the user has on the target node
                //   in that policy class
                ret += "The following section shows a more detailed permission breakdown from the perspective of each policy class:\n";
                Map<String, PolicyClass> policyClasses = explain.getPolicyClasses();
                int i = 1;
                for (String pcName : policyClasses.keySet()) {
                    ret += "\t" + i + ". '" + pcName + "':\n";
                    PolicyClass policyClass = policyClasses.get(pcName);

                    // the operations available to the user on the target under this policy class
                    Set<String> operations = policyClass.getOperations();
                    ret += "\t\t- Permissions (Given by this PC):\n";
                    for (String op: operations) {
                        ret += "\t\t\t- " + op + "\n";
                    }
                    // the paths from the user to the target
                    // A Path object contains the path and the permissions the path provides
                    // the path is just a list of nodes starting at the user and ending at the target node
                    // example: u1 -> ua1 -> oa1 -> o1 [read]
                    //   the association ua1 -> oa1 has the permission [read]
                    ret += "\t\t- Paths (How each permission is found):\n";
                    List<Path> paths = policyClass.getPaths();
                    for (Path path : paths) {
                        ret += "\t\t\t";
                        // this is just a list of nodes -> [u1, ua1, oa1, o1]
                        List<Node> nodes = path.getNodes();
                        for (Node n: nodes) {
                            ret += "'" + n.getName() + "'";
                            if (!nodes.get(nodes.size()-1).equals(n)) { // not final node
                                ret += " > ";
                            }
                        }

                        // this is the operations in the association between ua1 and oa1
                        Set<String> pathOps = path.getOperations();
                        ret += " " + pathOps;
                        // This is the string representation of the path (i.e. "u1-ua1-oa1-o1 ops=[r, w]")
                        String pathString = path.toString();
                        ret += "\n";
                    }
                    i++;
                }

                return ret;
            } else {
                return "Returned Audit was null";
            }
        } else {
            return "Either User ID or Target ID are null";
        }
    }
}
