package gov.nist.csd.pm.admintool.spt.parser;


import com.vaadin.flow.component.notification.Notification;
import gov.nist.csd.pm.admintool.graph.SingletonGraph;
import gov.nist.csd.pm.admintool.spt.common.SptToken;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.operations.OperationSet;
import gov.nist.csd.pm.pip.graph.model.nodes.Node;
import gov.nist.csd.pm.pip.graph.model.nodes.NodeType;
import gov.nist.csd.pm.pip.graph.model.relationships.Association;

import java.util.ArrayList;
import java.util.Iterator;



public class Rule1Parser extends SptRuleParser {

    // variables for PC1
    ArrayList<String> pc1uas = new ArrayList<String>();
    ArrayList<String> pc1oas = new ArrayList<String>();

    ArrayList<Node> pc1UaList = null;
    ArrayList<Node> pc1OaList = null;

    ArrayList<String> pcs1UA = new ArrayList<String>();
    ArrayList<String> pcs1OA = new ArrayList<String>();

    ArrayList<Node> pcsforUA1 = null;
    ArrayList<Node> pcsforOA1 = null;

    // Final variables
    ArrayList<Assignment> assignments = null;
    ArrayList<Association> associations = null;
    OperationSet associationOperations = null;

    public Rule1Parser() {
        super();
    }

    private class Assignment {
        Node fromNode;
        Node toNode;

        public Assignment(Node fromNode, Node toNode) {
            this.fromNode = fromNode;
            this.toNode = toNode;
        }

        public Assignment() {

        }
    }

    private class ruleAssociation {
        Node fromNode;
        Node toNode;
        String[] ops;

        public ruleAssociation() {
        }
        public ruleAssociation(Node fromNode, Node toNode, String[] ops) {
            this.fromNode = fromNode;
            this.toNode = toNode;
            this.ops = ops;
        }
    }


    // <rule1> ::= rule1 allow <uattr clause> <ops> <oattr clause>
    protected String rule1() throws Exception {
        String result = null;
        traceEntry("rule1");
        semopRule1Init();
        crtToken = myScanner.nextToken();
        if (SptToken.tokenId == SptRuleScanner.PM_ALLOW) {
            traceConsume();
            crtToken = myScanner.nextToken();
            result = uattrClause();
            if (result != null) {
                return result;
            }

            result = ops();
            if (result != null) {
                return result;
            }

            result = oattrClause();
            if (result != null) {
                return result;
            }
//            printElements();
            try {
                buildPolicy();
            } catch (PMException e) {
                e.printStackTrace();
            }
        } else return signalError(crtToken.tokenValue, SptRuleScanner.PM_ALLOW);
        return result;
    }
    public boolean testPolicy() {
        // 1. Create a user under UA
        // 2. Get permission ops on the OA and compare with the all the operations in the rule. If they match, return true else false

        return true;
    }
    // <uattr clause> ::= ua_name {< uattr assign>}:<pc list>
    private String uattrClause() {
        String result = null;
        traceEntry("uattrClause");

        if (SptToken.tokenId != SptRuleScanner.PM_WORD) {
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_WORD);
        }
        traceConsume();
        pc1uas.add(crtToken.tokenValue);
        crtToken = myScanner.nextToken();
        result = uattrAssign();

        if (SptToken.tokenId != SptRuleScanner.PM_COLON) {
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_COLON);
        }
        traceConsume();
        crtToken = myScanner.nextToken();
        result = pcListUA();
        if (result != null) {
            return result;
        }
        System.out.println("About to call semopUA()");
//        semopUA();
        return result;
    }

    // <uattr assign> ::= -> ua_name
    private String uattrAssign() {
        traceEntry("uattrAssign");
        while (true) {
            if (SptToken.tokenId == SptRuleScanner.PM_ARROW) {
                traceConsume();
                crtToken = myScanner.nextToken();
                if (SptToken.tokenId != SptRuleScanner.PM_WORD) {
                    return signalError(crtToken.tokenValue, SptRuleScanner.PM_WORD);
                }
                traceConsume();
                pc1uas.add(crtToken.tokenValue);
                crtToken = myScanner.nextToken();
            } // continue with the loop
            else break;
        }
        return null;
    }

    // <ops> ::= to op_name {, op_name}
    private String ops() {
        traceEntry("ops");

        traceEntry("ops");
        if (SptToken.tokenId != SptRuleScanner.PM_TO) {
            traceExit("ops");
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_TO);
        }
        traceConsume();
        crtToken = myScanner.nextToken();

        if (SptToken.tokenId != SptRuleScanner.PM_WORD) {
            traceExit("ops");
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_WORD);
        }
        traceConsume();
        associationOperations.add(crtToken.tokenValue);
        crtToken = myScanner.nextToken();
        while (true) {
            if (SptToken.tokenId != SptRuleScanner.PM_COMMA) {
                break;
            }
            traceConsume();
            crtToken = myScanner.nextToken();
            if (SptToken.tokenId != SptRuleScanner.PM_WORD) {
                traceExit("ops");
                return signalError(crtToken.tokenValue, SptRuleScanner.PM_WORD);
            }
            traceConsume();
            associationOperations.add(crtToken.tokenValue);
            crtToken = myScanner.nextToken();

        }
        traceExit("ops");
        return null;
    }

    // <oattr clause> ::= in oa_name {< oattr assign>}:<pc list>
    private String oattrClause(){
        String result = null;
        traceEntry("oattrClause");

        if (SptToken.tokenId != SptRuleScanner.PM_IN) {
            traceExit("oattrClause");
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_IN);
        }
        traceConsume();
        crtToken = myScanner.nextToken();

        if (SptToken.tokenId != SptRuleScanner.PM_WORD) {
            traceExit("oattrClause");
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_WORD);
        }
        traceConsume();
        pc1oas.add(crtToken.tokenValue);
        crtToken = myScanner.nextToken();
        result = oattrAssign();
        if (result != null) {
            traceExit("oattrClause");
            return result;
        }
        if (SptToken.tokenId != SptRuleScanner.PM_COLON) {
            traceExit("oattrClause");
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_COLON);
        }
        traceConsume();
        crtToken = myScanner.nextToken();
        result = pcListOA();
//        semopOA();

        if (result != null) {
            traceExit("oattrClause");
            return result;
        }
        return null;
    }

    // <oattr assign> ::= -> oa_name
    private String oattrAssign() {
        traceEntry("oattrAssign");
        while(true) {
            if (SptToken.tokenId == SptRuleScanner.PM_ARROW) {
                traceConsume();
                crtToken = myScanner.nextToken();
                if (SptToken.tokenId != SptRuleScanner.PM_WORD) {
                    return signalError(crtToken.tokenValue, SptRuleScanner.PM_WORD);
                }
                traceConsume();
                pc1oas.add(crtToken.tokenValue);
                crtToken = myScanner.nextToken();
            } else break;
        }
        return null;
    }

    // <pc list> ::= pc_name {, pc_name}
    private String pcListUA() {
        traceEntry("pcListUA");

        if (SptToken.tokenId != SptRuleScanner.PM_WORD) {
            traceExit("pcListUA");
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_WORD);
        }
        traceConsume();
        pcs1UA.add(crtToken.tokenValue);
        crtToken = myScanner.nextToken();
        while (true) {
            if (SptToken.tokenId != SptRuleScanner.PM_COMMA) {
                break;
            }
            traceConsume();
            crtToken = myScanner.nextToken();
            if (SptToken.tokenId != SptRuleScanner.PM_WORD) {
                traceExit("pcList");
                return signalError(crtToken.tokenValue, SptRuleScanner.PM_WORD);
            }
            traceConsume();
            pcs1UA.add(crtToken.tokenValue);
            crtToken = myScanner.nextToken();
        }
        traceExit("pcList");
        return null;
    }

    // <pc list> ::= pc_name {, pc_name}
    private String pcListOA() {
        traceEntry("pcList");

        if (SptToken.tokenId != SptRuleScanner.PM_WORD) {
            traceExit("pcList");
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_WORD);
        }
        traceConsume();
        pcs1OA.add(crtToken.tokenValue);
        crtToken = myScanner.nextToken();
        while (true) {
            if (SptToken.tokenId != SptRuleScanner.PM_COMMA) {
                break;
            }
            traceConsume();
            crtToken = myScanner.nextToken();
            if (SptToken.tokenId != SptRuleScanner.PM_WORD) {
                traceExit("pcList");
                return signalError(crtToken.tokenValue, SptRuleScanner.PM_WORD);
            }
            traceConsume();
            pcs1OA.add(crtToken.tokenValue);
            crtToken = myScanner.nextToken();
        }
        traceExit("pcList");
        return null;
    }

    private void printElements() {
        System.out.println("=======================================================================================");
        System.out.println("PC 1 UAs are ...");
        for (int i=0;i<pc1UaList.size();i++) {
            System.out.println(pc1UaList.get(i).getName());
        }
        System.out.println("PC 1 OAs are ...");
        for (int i=0;i<pc1OaList.size();i++) {
            System.out.println(pc1OaList.get(i).getName());
        }
        System.out.println("Assignments are ...");
        for (int i=0;i<assignments.size();i++) {
            System.out.println(assignments.get(i).fromNode.getName()+"-"+assignments.get(i).toNode.getName());
        }
        System.out.println("Associations are ...");
        for (int i=0;i<associations.size();i++) {
            System.out.println(associations.get(i).getSource()+"-"+associations.get(i).getTarget());
        }

        System.out.println("Operations are ...");
        Iterator<String> it = associationOperations.iterator();
        while(it.hasNext()){
            System.out.println(it.next());
        }
        System.out.println("=======================================================================================");
    }

    // semop methds
    private void semopRule1Init() {
        // create necessary structure to store grammar values
        pc1UaList = new ArrayList<>();
        pc1OaList = new ArrayList<>();
        pcsforUA1 = new ArrayList<>();
        pcsforOA1 = new ArrayList<>();
        associations = new ArrayList<>();
        associationOperations = new OperationSet();
    }

    /* Graph updates
        0. Reset graph
        1. create Nodes
        2. Create Assignments
        3. Create associations
    */
    private void buildPolicy() throws PMException {
        SingletonGraph graph = SingletonGraph.getInstance();
//        graph.reset();
        int pc1UASize = pc1uas.size();
        int pc1OASize = pc1oas.size();
        System.out.println("Build PCs..." + pcs1UA.size() + " PCs");
        for (int i=0;i<pcs1UA.size();i++) {
            pcsforUA1.add(graph.createPolicyClass(pcs1UA.get(0), null));
        }

        System.out.println("Build PC1 UAs ..." + "for PC " + pcsforUA1.get(0).getName());
        // First parent UA
        String parentUAId = graph.getPolicyClassDefault(pcsforUA1.get(0).getName(),NodeType.UA);
        pc1UaList.add(graph.createNode(pc1uas.get(pc1UASize-1), NodeType.UA,null, parentUAId ));
        for (int i=1;i<pcsforUA1.size();i++) {
            graph.assign(pc1UaList.get(pc1UASize-1).getName(), pcsforUA1.get(i).getName());
        }
        // Other UAs
        for (int i=pc1UASize-2;i>=0;i--) {
            pc1UaList.add(graph.createNode(pc1uas.get(i), NodeType.UA,null, pc1UaList.get(pc1UaList.size()-1).getName()));
        }
        // First parent OA
        String parentOA = graph.getPolicyClassDefault(pcsforUA1.get(0).getName(), NodeType.OA);
        System.out.println("Build PC 1 OAs ...");
        pc1OaList.add(graph.createNode(pc1oas.get(pc1OASize-1), NodeType.OA,null, parentOA));
        for (int i=1;i<pcsforUA1.size();i++) {
            graph.assign(pc1OaList.get(pc1OASize-1).getName(), pcsforUA1.get(i).getName());
        }
        // Other OAs
        for (int i=pc1OASize-2;i>=0;i--) {
            pc1OaList.add(graph.createNode( pc1oas.get(i), NodeType.OA,null, pc1OaList.get(pc1OaList.size()-1).getName()));
        }

        // Build Associations
        System.out.println("Building association between " + pc1UaList.get(pc1UASize-1).getName() + " and " + pc1OaList.get(pc1OASize-1).getName());
        graph.associate(pc1UaList.get(pc1UASize-1).getName(), pc1OaList.get(pc1OASize-1).getName(), associationOperations );
    }

    public void notify(String message){
        Notification notif = new Notification(message, 3000);
        notif.open();
    }
}
