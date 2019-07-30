package gov.nist.csd.pm.admintool.tests;

import gov.nist.csd.pm.admintool.graph.SingletonGraph;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.pip.graph.model.nodes.Node;

import java.util.Map;
import java.util.Set;

public class AssertAssociation extends Test {
    public AssertAssociation() {
        setName("assertAssociation");
        addParam("uaID", Type.NODETYPE);
        addParam("targetID", Type.NODETYPE);
        addParam("op", Type.OPERATION);
    }

    public void setUaID(long uaID) {
        setParamValue("uaID", uaID);
    }

    public void setTargetID(long targetID) {
        setParamValue("targetID", targetID);
    }

    public void setOp(String op) {
        setParamValue("op", op);
    }

    @Override
    public boolean runTest() {
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
}
