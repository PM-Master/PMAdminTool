package gov.nist.csd.pm.admintool.tests;

import gov.nist.csd.pm.admintool.graph.SingletonGraph;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.pip.graph.model.nodes.Node;

import java.util.Set;

public class AssertAssignment extends Test {
    public AssertAssignment() {
        setName("assertAssociation");
        addParam("childID", Type.NODETYPE);
        addParam("parentID", Type.NODETYPE);
    }

    @Override
    public boolean runTest() {
        Node child = ((Node)getParams().get("childID").getValue());
        Node parent = ((Node)getParams().get("parentID").getValue());

        if (child != null && parent != null) {
            SingletonGraph g = SingletonGraph.getInstance();
            try {
                Set<Node> children = g.getChildren(parent.getID());
                for (Node childNode: children){
                    if (childNode.equals(child)) {
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
}
