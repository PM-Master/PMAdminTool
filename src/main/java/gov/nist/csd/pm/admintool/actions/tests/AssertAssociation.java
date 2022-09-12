package gov.nist.csd.pm.admintool.actions.tests;

import gov.nist.csd.pm.admintool.graph.SingletonClient;
import gov.nist.csd.pm.policy.exceptions.PMException;
import gov.nist.csd.pm.policy.model.access.AccessRightSet;
import gov.nist.csd.pm.policy.model.graph.nodes.Node;
import gov.nist.csd.pm.policy.model.graph.relationships.Association;

import java.util.List;
import java.util.Map;

public class AssertAssociation extends Test {
    public AssertAssociation() {
        setName("assertAssociation");
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
            SingletonClient g = SingletonClient.getInstance();
            try {
                List<Association> sourceAssociations = g.getSourceAssociations(uaID.getName());
                for (Association tID: sourceAssociations){
                    if (tID.getTarget().equalsIgnoreCase(targetID.getName())) {
                        return tID.getAccessRightSet().contains(operation);
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
