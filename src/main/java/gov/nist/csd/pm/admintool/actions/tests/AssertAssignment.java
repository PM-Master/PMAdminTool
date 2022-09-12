package gov.nist.csd.pm.admintool.actions.tests;

import gov.nist.csd.pm.admintool.graph.SingletonClient;
import gov.nist.csd.pm.policy.exceptions.PMException;
import gov.nist.csd.pm.policy.model.graph.nodes.Node;

import java.util.Set;

public class AssertAssignment extends Test {
    public AssertAssignment() {
        setName("assertAssociation");
        addParam("childID", Type.NODETYPE);
        addParam("parentID", Type.NODETYPE);
    }

    @Override
    public boolean run() {
        Node child = ((Node)getParams().get("childID").getValue());
        Node parent = ((Node)getParams().get("parentID").getValue());

        if (child != null && parent != null) {
            SingletonClient g = SingletonClient.getInstance();
            try {
                Set<String> children = g.getChildren(parent.getName());
                for (String childNode: children){
                    if (childNode.equalsIgnoreCase(child.getName())) {
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
        Node child = ((Node)getParams().get("childID").getValue());
        Node parent = ((Node)getParams().get("parentID").getValue());
        if (child == null) {
            if (parent == null) {
                return "Both child and parent are null";
            } else {
                return "Child is null";
            }
        } else {
            if (parent == null) {
                return "Parent is null";
            } else {
                if (!run()) {
                    return "There is no assignment between '" + child.getName() + "' and '" + parent.getName() + "'";
                } else {
                    return "There is an assignment between '" + child.getName() + "' and '" + parent.getName() + "'";
                }
            }
        }
    }
}
