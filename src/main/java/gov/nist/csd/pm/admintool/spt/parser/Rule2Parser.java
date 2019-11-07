package gov.nist.csd.pm.admintool.spt.parser;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import gov.nist.csd.pm.admintool.graph.SingletonGraph;
import gov.nist.csd.pm.admintool.spt.common.SptToken;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.pip.graph.model.nodes.Node;
import gov.nist.csd.pm.pip.graph.model.nodes.NodeType;
import gov.nist.csd.pm.pip.graph.model.relationships.Association;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class Rule2Parser extends SptRuleParser {

    // variables for PC1
    ArrayList<String> pc1uas = new ArrayList<String>();
    ArrayList<String> pc1oas = new ArrayList<String>();

    ArrayList<Node> pc1UaList = null;
    ArrayList<Node> pc1OaList = null;

    ArrayList<String> pcs1UA = new ArrayList<String>();
    ArrayList<String> pcs1OA = new ArrayList<String>();

    ArrayList<Node> pcsforUA1 = null;
    ArrayList<Node> pcsforOA1 = null;

    // variables for PC2

    ArrayList<String> pc2uas = new ArrayList<String>();
    ArrayList<String> pc2oas = new ArrayList<String>();

    ArrayList<Node> pc2UaList = null;
    ArrayList<Node> pc2OaList = null;

    String pc2;
    Node pc2Node;

    // Final variables
    ArrayList<Assignment> assignments = null;
    ArrayList<Association> associations = null;
    Set<String> associationOperations = null;

    ArrayList<Purpose> purpose = null;

    public Rule2Parser() {
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

    SingletonGraph graph = SingletonGraph.getInstance();

    public class Purpose {
        Node fromNode;
        Node toNode;
        Set<String> ops;

        public Purpose() {
        }
        public Purpose(Node fromNode, Set<String> ops, Node toNode) {
            this.fromNode = fromNode;
            this.toNode = toNode;
            this.ops = ops;
        }
    }


    // <rule2> ::= rule2 <allow clause> <when clause>
    protected String rule2() throws Exception {
        String result = null;
        traceEntry("rule2");

        if (SptToken.tokenId == SptRuleScanner.PM_RULE2) {
            traceConsume();
            semopRule2Init();
            crtToken = myScanner.nextToken();
            result = allowClause();
            if (result != null) {
                return result;
            }

            result = whenClause();

            if (result != null) {
                return result;
            }
            return result;
        } else return signalError(crtToken.tokenValue, SptRuleScanner.PM_RULE2);
    }

    // <allow clause> ::= allow <uattr clause> <ops> <oattr clause>
    private String allowClause() {
        String result = null;
        traceEntry("allowClause");

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
        } else return signalError(crtToken.tokenValue, SptRuleScanner.PM_ALLOW);
        return result;
    }

    // <uattr clause> ::= ua_name {< uattr assign>}:<pc list>; ask ua value:pc_name
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

        if (SptToken.tokenId != SptRuleScanner.PM_SEMICOLON) {
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_SEMICOLON);
        }
        traceConsume();
        crtToken = myScanner.nextToken();

        if (SptToken.tokenId != SptRuleScanner.PM_ASK) {
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_ASK);
        }
        traceConsume();
        crtToken = myScanner.nextToken();
        if (SptToken.tokenId != SptRuleScanner.PM_UA) {
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_UA);
        }
        traceConsume();
        crtToken = myScanner.nextToken();
        if (SptToken.tokenId != SptRuleScanner.PM_VALUE) {
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_VALUE);
        }
        traceConsume();
        crtToken = myScanner.nextToken();
        if (SptToken.tokenId != SptRuleScanner.PM_COLON) {
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_COLON);
        }
        traceConsume();
        crtToken = myScanner.nextToken();
        if (SptToken.tokenId != SptRuleScanner.PM_WORD) {
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_WORD);
        }
        traceConsume();
        pc2 =  crtToken.tokenValue;
        crtToken = myScanner.nextToken();
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

    // <oattr clause> ::= in oa_name {< oattr assign>}:<pc list>; ask oa value:pc_name
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

        if (SptToken.tokenId != SptRuleScanner.PM_SEMICOLON) {
            traceExit("oattrClause");
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_SEMICOLON);
        }
        traceConsume();
        crtToken = myScanner.nextToken();
        if (SptToken.tokenId != SptRuleScanner.PM_ASK) {
            traceExit("oattrClause");
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_ASK);
        }
        traceConsume();
        crtToken = myScanner.nextToken();
        if (SptToken.tokenId != SptRuleScanner.PM_OA) {
            traceExit("oattrClause");
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_OA);
        }
        traceConsume();
        crtToken = myScanner.nextToken();

        if (SptToken.tokenId != SptRuleScanner.PM_VALUE) {
            traceExit("oattrClause");
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_VALUE);
        }
        traceConsume();
        crtToken = myScanner.nextToken();

        if (SptToken.tokenId != SptRuleScanner.PM_COLON) {
            traceExit("oattrClause");
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_COLON);
        }
        traceConsume();
        crtToken = myScanner.nextToken();
        if (SptToken.tokenId != SptRuleScanner.PM_WORD) {
            traceExit("oattrClause");
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_WORD);
        }
        traceConsume();
        crtToken = myScanner.nextToken();

        traceExit("oattrClause");
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

    //<when clause> ::= when ua_value = oa_value
    private String whenClause() {
        traceEntry("whenClause");
        if (SptToken.tokenId != SptRuleScanner.PM_WHEN) {
            traceExit("whenClause");
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_WHEN);
        }
        traceConsume();
        crtToken = myScanner.nextToken();

        if (SptToken.tokenId != SptRuleScanner.PM_WORD) {
            traceExit("whenClause");
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_WORD);
        }
        traceConsume();
        crtToken = myScanner.nextToken();

        if (SptToken.tokenId != SptRuleScanner.PM_EQUAL) {
            traceExit("whenClause");
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_EQUAL);
        }
        traceConsume();
        crtToken = myScanner.nextToken();

        if (SptToken.tokenId != SptRuleScanner.PM_WORD) {
            traceExit("whenClause");
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_WORD);
        }
        traceConsume();
        crtToken = myScanner.nextToken();
        userDialogue();
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
        System.out.println("PC 2 UAs are ...");
        for (int i=0;i<pc2UaList.size();i++) {
            System.out.println(pc2UaList.get(i).getName());
        }
        System.out.println("PC 2 OAs are ...");
        for (int i=0;i<pc2OaList.size();i++) {
            System.out.println(pc2OaList.get(i).getName());
        }
        System.out.println("Assignments are ...");
        for (int i=0;i<assignments.size();i++) {
            System.out.println(assignments.get(i).fromNode.getName()+"-"+assignments.get(i).toNode.getName());
        }
        System.out.println("Associations are ...");
        for (int i=0;i<associations.size();i++) {
            System.out.println(associations.get(i).getSourceID()+"-"+associations.get(i).getTargetID());
        }

        System.out.println("Operations are ...");
        Iterator<String> it = associationOperations.iterator();
        while(it.hasNext()){
            System.out.println(it.next());
        }
        System.out.println("=======================================================================================");
    }

    // semop methds
    private void semopRule2Init() {
        // create necessary structure to store grammar values
        pc1UaList = new ArrayList<Node>();
        pc1OaList = new ArrayList<Node>();
        pcsforUA1 = new ArrayList<Node>();
        pcsforOA1 = new ArrayList<Node>();
        pc2UaList = new ArrayList<Node>();
        pc2OaList = new ArrayList<Node>();
        assignments = new ArrayList<Assignment>();
        associations = new ArrayList<Association>();
        associationOperations = new HashSet<>();
    }

    /* semopUA() - Add from uas to Nodes
        1. Add to nodes - uas, pcs
        2. Add to assignment - uas-> pc
    */
    private void semopUA(){
        Assignment assignment = new Assignment();
        // add UAs
        for (int i=0;i<=pc1uas.size()-1;i++) {
            Node node = new Node(generateRandomId(), pc1uas.get(i), NodeType.toNodeType("UA"),null);
            pc1UaList.add(node);
        }
        // Add PCs
        for (int i=0;i<=pcs1UA.size()-1;i++) {
            Node node = new Node(generateRandomId(), pcs1UA.get(i), NodeType.toNodeType("PC"),null);
            pcsforUA1.add(node);
        }
        // Add Assignments in each PC
        for (int i=0;i<pcsforUA1.size();i++) {

            for (int j=0;j<=pc1UaList.size()-2;j++) {
                assignment = new Assignment(pc1UaList.get(j),pc1UaList.get(j+1));
                assignments.add(assignment);
            }
            assignment = new Assignment(pc1UaList.get(pc1UaList.size()-1),pcsforUA1.get(i));
            assignments.add(assignment);
        }
    }

    /* semopOA() - Add from oas to Nodes
            1. Add to nodes - oas, pcs
            2. Add to assignment - oas-> pc
        */
    private void semopOA(){
        Assignment assignment = new Assignment();

        for (int i=0;i<=pc1oas.size()-1;i++) {
            Node node = new Node(generateRandomId(), pc1oas.get(i), NodeType.toNodeType("OA"),null);
            pc1OaList.add(node);
        }
        for (int i=0;i<=pcs1OA.size()-1;i++) {
            Node node = new Node(generateRandomId(), pcs1OA.get(i), NodeType.toNodeType("PC"),null);
            pcsforOA1.add(node);
        }
        for (int i=0;i<pcsforOA1.size();i++) {

            for (int j=0;j<=pc1OaList.size()-2;j++) {
                assignment = new Assignment(pc1OaList.get(j),pc1OaList.get(j+1));
                assignments.add(assignment);
            }
            assignment = new Assignment(pc1OaList.get(pc1OaList.size()-1),pcsforOA1.get(i));
            assignments.add(assignment);
        }
//        semopAssociations();
    }
    // Add to Associations
    private void semopAssociations() {
        Association association = new Association(pc1UaList.get(0).getID(),pc1OaList.get(0).getID(),associationOperations);
        associations.add(association);
    }
    /*
        1. Prompt user to enter uas-oa value pairs
        2. Add to uaList and oaList
        3. Add to assignments
        4. Add to Associations
         */
    private void semopDynamicRelations() {
        // add PC first
        Node pc2Node = new Node(generateRandomId(), pc2, NodeType.toNodeType("PC"),null);
        for (int i=0;i<pc2uas.size();i++) {
            System.out.println(pc2uas.get(i));
            Node node = new Node(generateRandomId(), pc2uas.get(i), NodeType.toNodeType("UA"),null);
            pc2UaList.add(node);
            Assignment assignment = new Assignment(node, pc2Node);
            assignments.add(assignment);
        }
        for (int i=0;i<pc2oas.size();i++) {
            Node node = new Node(generateRandomId(), pc2oas.get(i), NodeType.toNodeType("OA"),null);
            pc2OaList.add(node);
            Assignment assignment = new Assignment(node, pc2Node);
            assignments.add(assignment);
        }
        Association association;
        for (int i=0;i<pc2UaList.size();i++) {
            association = new Association(pc2UaList.get(i).getID(), pc2OaList.get(i).getID(), associationOperations);
            associations.add(association);
        }
    }

    private void userDialogue() {
        // creating the dialog

        Dialog dialog = new Dialog();
        HorizontalLayout form = new HorizontalLayout(); // form will hold the text area
        form.setAlignItems(FlexComponent.Alignment.BASELINE);

        // creating the text input area
        TextArea attributesFeild = new TextArea("Attributes (ua=oa \\n...)");
        attributesFeild.setPlaceholder("Enter Attributes...");
        form.add(attributesFeild);

        // button with parsing
        Button button = new Button("Submit", event -> {
            String attrsString = attributesFeild.getValue();
            if (attrsString != null && !attrsString.equals("")) {
                try {
                    for (String attrLine : attrsString.split("\n")) {
                        String[] attrs = attrLine.split("=");
                        System.out.println("Adding uas in the list for pc2: " + attrs[0] + "=" + attrs[1]);
                        pc2uas.add(attrs[0]);
                        pc2oas.add(attrs[1]);
                    }
                    dialog.close();
                } catch (Exception e) {
                    this.notify("Incorrect Formatting of Attributes");
                    e.printStackTrace();
                }
                printElements();
//                semopDynamicRelations();
                try {
                    buildPolicy();
                } catch (PMException e) {
                    e.printStackTrace();
                }

            } else {
                this.notify("Empty Attributes");
            }
        });
        form.add(button);

        // adding overall form and opening dialog
        dialog.add(form);
        dialog.open();
        attributesFeild.focus();
    }

    /* Graph updates
        0. Reset graph
        1. create Nodes
        2. Create Assignments
        3. Create associations
    */
    private void buildPolicy() throws PMException {

        ///// for PC1
//        graph.reset();
        long id=generateRandomId();
        int pc1UASize = pc1uas.size();
        int pc1OASize = pc1oas.size();
        System.out.println("Build PCs..." + pcs1UA.size() + " PCs");
        for (int i=0;i<pcs1UA.size();i++) {
            pcsforUA1.add(graph.createPolicyClass(pcs1UA.get(0), null));
        }

        System.out.println("Build PC1 UAs ..." + "for PC " + pcsforUA1.get(0).getID());
        // First parent UA
        Long parentUAId = graph.getPolicyClassDefault(pcsforUA1.get(0).getID(),NodeType.UA);
        pc1UaList.add(graph.createNode(parentUAId, pc1uas.get(pc1UASize-1), NodeType.UA,null));
        for (int i=1;i<pcsforUA1.size();i++) {
            graph.assign(pc1UaList.get(pc1UASize-1).getID(), pcsforUA1.get(i).getID());
        }
        // Other UAs
        for (int i=pc1UASize-2;i>=0;i--) {
            pc1UaList.add(graph.createNode(pc1UaList.get(pc1UaList.size()-1).getID(), pc1uas.get(i), NodeType.UA,null));
        }
        // First parent OA
        Long parentOAId = graph.getPolicyClassDefault(pcsforUA1.get(0).getID(),NodeType.OA);
        System.out.println("Build PC 1 OAs ...");
        pc1OaList.add(graph.createNode(parentOAId, pc1oas.get(pc1OASize-1), NodeType.OA,null));
        for (int i=1;i<pcsforUA1.size();i++) {
            graph.assign(pc1OaList.get(pc1OASize-1).getID(), pcsforUA1.get(i).getID());
        }
        // Other OAs
        for (int i=pc1OASize-2;i>=0;i--) {
            pc1OaList.add(graph.createNode(pc1OaList.get(pc1OaList.size()-1).getID(), pc1oas.get(i), NodeType.OA,null));
        }

        // Build Associations for PC1
        System.out.println("Building association between " + pc1UaList.get(pc1UASize-1).getName() + " and " + pc1OaList.get(pc1OASize-1).getName());
        graph.associate(pc1UaList.get(pc1UASize-1).getID(), pc1OaList.get(pc1OASize-1).getID(), associationOperations );

        // for PC2

        System.out.println("Building PC2 ...");
        // dummy id. PC is created but parent id is ignored
        Node pc2Node = graph.createPolicyClass(pc2,null);
        // Build UAs
        System.out.println("Building PC 2 UAs ...");
        parentUAId = graph.getPolicyClassDefault(pc2Node.getID(),NodeType.UA);
        for (int i=0;i<pc2uas.size();i++) {
            pc2UaList.add(graph.createNode(parentUAId, pc2uas.get(i), NodeType.UA,null));
        }
        //Build OAs
        System.out.println("Building PC 2 OAs ...");
        parentOAId = graph.getPolicyClassDefault(pc2Node.getID(),NodeType.OA);
        for (int i=0;i<pc2oas.size();i++) {
            pc2OaList.add(graph.createNode(parentOAId, pc2oas.get(i), NodeType.OA,null));
        }

        // Build Associations
        System.out.println("Building Associations ...");

        for (int i=0;i<pc2UaList.size();i++) {
            graph.associate(pc2UaList.get(i).getID(), pc2OaList.get(i).getID(), associationOperations );
        }
        buildPurpose();
    }

    public void buildPurpose() {
        Purpose p;
        for (Node n1: pc1UaList)    {
            for (Node n2: pc1OaList) {
                p = new Purpose(n1, associationOperations, n2);
                purpose.add(p);
            }
        }

        for (Node n1: pc2UaList)    {
            for (Node n2: pc2OaList) {
                p = new Purpose(n1, associationOperations, n2);
                purpose.add(p);
            }
        }
    }

    public void analyze(Purpose p) {
        // Analyze the permissions
        String paths = explain(p.fromNode, p.toNode);
        System.out.println(paths);
        // fix the policy dynamically
        fixPolicyDynamically();
    }

    public void fixPolicyDynamically() {

    }
    public void notify(String message){
        Notification notif = new Notification(message, 3000);
        notif.open();
    }
}
