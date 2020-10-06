package gov.nist.csd.pm.admintool.graph.customObligationFunctions;

import gov.nist.csd.pm.epp.FunctionEvaluator;
import gov.nist.csd.pm.epp.events.AssignToEvent;
import gov.nist.csd.pm.epp.events.EventContext;
import gov.nist.csd.pm.epp.functions.FunctionExecutor;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.operations.OperationSet;
import gov.nist.csd.pm.pdp.PDP;
import gov.nist.csd.pm.pdp.services.UserContext;
import gov.nist.csd.pm.pip.graph.Graph;
import gov.nist.csd.pm.pip.graph.model.nodes.Node;
import gov.nist.csd.pm.pip.graph.model.nodes.NodeType;
import gov.nist.csd.pm.pip.obligations.model.functions.Function;

public class RecordFunctionExecutor implements FunctionExecutor {
    @Override
    public String getFunctionName() {
        return "record_function";
    }

    @Override
    public int numParams() {
        return 1;
    }

    @Override
    public Object exec(UserContext userContext, EventContext eventContext, PDP pdp, Function function, FunctionEvaluator functionEvaluator) throws PMException {
        AssignToEvent event = (AssignToEvent)eventContext;
        Node childNode = event.getChildNode();

        Graph graphService = pdp.getGraphService(userContext);
        if (childNode.getType().equals(NodeType.UA)) {
            graphService.createNode(childNode.getName().substring(0, childNode.getName().length() - 3), NodeType.U, null, childNode.getName());
            Node user_record = graphService.createNode(childNode.getName() + " record", NodeType.OA, null, "Patients Records");
            graphService.associate(childNode.getName(), user_record.getName(), new OperationSet("read"));
        }

        return null;
    }
}
