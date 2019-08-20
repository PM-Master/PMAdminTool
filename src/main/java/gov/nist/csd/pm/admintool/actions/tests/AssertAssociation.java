package gov.nist.csd.pm.admintool.actions.tests;

import gov.nist.csd.pm.admintool.graph.SingletonGraph;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.pip.graph.model.nodes.Node;

import java.util.Map;
import java.util.Set;

public class AssertAssociation extends Test {
    public AssertAssociation() {
        setName("assertAssignment");
        addParam("uaID", Type.NODETYPE);
        addParam("targetID", Type.NODETYPE);
        addParam("op", Type.OPERATION);
    }

    @Override
    public boolean run() {
        Node uaID = ((Node)getParams().get("uaID").getValue());
        Node targetID = ((Node)getParams().get("targetID").getValue());
        String operation = (String)getParams().get("op").getValue();

        if (uaID != null && targetID != null && operation != null) {
            SingletonGraph g = SingletonGraph.getInstance();
            try {
                Map<Long, Set<String>> sourceAssociations = g.getSourceAssociations(uaID.getID());
                for (long tID: sourceAssociations.keySet()){
                    if (tID == targetID.getID()) {
                        return sourceAssociations.get(tID).contains(operation);
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
        Node uaID = ((Node)getParams().get("uaID").getValue());
        Node targetID = ((Node)getParams().get("targetID").getValue());
        String operation = (String)getParams().get("op").getValue();
        if (uaID == null) {
            if (targetID == null) {
                if (operation == null) {
                    return "User Attribute, Target ID, and Operation are null";
                } else {
                    return "User Attribute, and Target ID, are null";
                }
            } else {
                if (operation == null) {
                    return "User Attribute, and Operation are null";
                } else {
                    return "User Attribute is null";
                }
            }
        } else {
            if (targetID == null) {
                if (operation == null) {
                    return "Target ID, and Operation are null";
                } else {
                    return "Target ID is null";
                }
            } else {
                if (operation == null) {
                    return "Operation is null";
                } else {
                    if (!run()) {
                        return "The '" + operation + "' operation does not exist between '" + uaID.getName() + "' and '" + targetID.getName() + "'";
                    } else {
                        return "The '" + operation + "' operation exists between '" + uaID.getName() + "' and '" + targetID.getName() + "'";
                    }
                }
            }
        }
    }
}
