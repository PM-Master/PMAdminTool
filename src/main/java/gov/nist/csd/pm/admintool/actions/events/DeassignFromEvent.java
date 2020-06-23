package gov.nist.csd.pm.admintool.actions.events;

import com.vaadin.flow.component.notification.Notification;
import gov.nist.csd.pm.admintool.graph.SingletonGraph;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.pdp.services.UserContext;
import gov.nist.csd.pm.pip.graph.model.nodes.Node;

public class DeassignFromEvent extends Event {
    String explanation;
    public DeassignFromEvent() {
        setName("deassignFromEvent");
        addParam("parentID", Type.NODETYPE);
        addParam("childID", Type.NODETYPE);
        explanation = null;
    }

    @Override
    public boolean run() {
        Node child = ((Node)getParams().get("childID").getValue());
        Node parent = ((Node)getParams().get("parentID").getValue());
        SingletonGraph g = SingletonGraph.getInstance();
        if (child == null || parent == null) {
            if (g == null) {
                explanation = "";
            } else {
                explanation = "";
            }
            return false;
        }

        try {
            UserContext userContext = new UserContext(child.getName(), parent.getName());
            g.processEvent(new gov.nist.csd.pm.epp.events.DeassignFromEvent(userContext, parent, child));
//            notify("Deassignment from Event successfully executed");
            explanation = "Deassignment from Event successfully executed";
        } catch (PMException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            explanation = e.getMessage();
            return false;
        }
        return true;
    }

    @Override
    public String explain() {
        if (explanation == null) {
            Node child = ((Node)getParams().get("childID").getValue());
            Node parent = ((Node)getParams().get("parentID").getValue());
            SingletonGraph g = SingletonGraph.getInstance();
            if (child == null || parent == null) {
                if (g == null) {
                    explanation = "In Memory Graph is null";
                } else {
                    explanation = "At least one of the parameters are null";
                }
            } else {
                explanation = "None of the inputs are incorrect";
            }
        }
        return explanation;
    }

    public void notify(String message){
        Notification notif = new Notification(message, 3000);
        notif.open();
    }
}
