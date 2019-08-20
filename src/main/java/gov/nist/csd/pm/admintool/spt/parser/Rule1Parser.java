package gov.nist.csd.pm.admintool.spt.parser;

import gov.nist.csd.pm.admintool.graph.SingletonGraph;
import gov.nist.csd.pm.admintool.spt.common.PMElement;
import gov.nist.csd.pm.admintool.spt.common.RandomGUID;
import gov.nist.csd.pm.admintool.spt.common.SptToken;
import gov.nist.csd.pm.common.Operations;
import gov.nist.csd.pm.pip.graph.GraphSerializer;
import gov.nist.csd.pm.pip.graph.model.nodes.Node;
import gov.nist.csd.pm.pip.graph.model.nodes.NodeType;
import gov.nist.csd.pm.pip.graph.model.relationships.Assignment;
import gov.nist.csd.pm.pip.graph.model.relationships.Association;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Rule1Parser extends SptRuleParser{

    String rule1_pc = "";
    String rule1_ua = "";
    String rule1_attr = "";
    String rule1_attrIn = "";
    String uattr = "";
    String uattrContainer = "";
    ArrayList<String> uaWhenElements = new ArrayList<String>();
    String parent;
    String parentType;
    SingletonGraph g; // = SingletonGraph.getInstance();

    Node associationSource;
    ArrayList<Node> associationTargets = new ArrayList<Node>();
    Set<String> associationOperations = new HashSet<>();

    public static Long nodeid = 1L;

    public Rule1Parser() {
    }

    // <rule1> ::= rule1 <when clause> <allow clause>
    protected String rule1() throws Exception {
        String result = null;
        traceEntry("rule1");

        if (crtToken.tokenId == SptRuleScanner.PM_RULE1) {
            traceConsume();
            crtToken = myScanner.nextToken();
            semopRule1Init();
            result = whenClause1();
            if (result != null) {
                return result;
            }

            result = allowClause1();

            if (result != null) {
                return result;
            }
            return result;
        } else return signalError(crtToken.tokenValue, SptRuleScanner.PM_RULE1);
    }

    // <allow clause> := allow user <access rights> <on Whats>
    private String allowClause1() throws Exception {
        String result=null;
        traceEntry("allowClause1");
        if (crtToken.tokenId != SptRuleScanner.PM_ALLOW) {
            traceExit("allowClause1");
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_ALLOW);
        }
        traceConsume();
        crtToken = myScanner.nextToken();
        if (crtToken.tokenId != SptRuleScanner.PM_USER) {
            traceExit("allowClause1");
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_USER);
        }
        traceConsume();
        crtToken = myScanner.nextToken();

        result = accessRights();
        if (result != null) {
            traceExit("allowClause1");
            return result;
        }

        result = onWhats();
        if (result != null) {
            traceExit("assignAction");
            return result;
        }
        return result;
    }

    // <access rights> ::= <access right> {, <access right>}
    private String accessRights() throws Exception {
        String result = null;

        traceEntry("accessRights");
        while (true) {
            if (crtToken.tokenId != SptRuleScanner.PM_WORD) {
                traceExit("accessRights");
                return signalError(crtToken.tokenValue, SptRuleScanner.PM_WORD);
            }
            result = accessRight();
            if (result != null) {
                traceExit("accessRights");
                return result;
            }
            if (crtToken.tokenId != SptRuleScanner.PM_COMMA) {
                break;
            }
            traceConsume();
            crtToken = myScanner.nextToken();
        }
        traceExit("accessRights");
        return null;
    }

    // <access right> ::= ar_name
    private String accessRight() throws Exception {
        traceEntry("accessRight");
        if (crtToken.tokenId != SptRuleScanner.PM_WORD) {
            traceExit("accessRight");
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_WORD);
        }

        traceConsume();
        semopAnAR();
        crtToken = myScanner.nextToken();

        traceExit("accessRight");
        return null;
    }

    private void semopAnAR() throws Exception{
        associationOperations.add(crtToken.tokenValue);
    }

    // <on whats> ::= on <what> {, <what> }
    private String onWhats() throws Exception {
        String result = null;
        traceEntry("onWhats");
        if (crtToken.tokenId != SptRuleScanner.PM_ON) {
            traceExit("onWhats");
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_ON);
        }
        traceConsume();
        crtToken = myScanner.nextToken();
        while (true) {
            break;
        }
        result = what();
        if (result != null) {
            traceExit("onWhats");
            return result;
        }
        if (crtToken.tokenId != SptRuleScanner.PM_COMMA) {
            traceConsume();
            crtToken = myScanner.nextToken();
        }

        traceExit("onWhats");
        return null;
    }

    // <what> ::= user attribute ua_name [in ua_name] | object attribute oa_name [in oa_name]
    private String what() throws Exception {
        boolean ua = true;

        traceEntry("what");
        if (crtToken.tokenId != SptRuleScanner.PM_USER && crtToken.tokenId != SptRuleScanner.PM_OBJECT) {
            traceExit("what");
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_USER, SptRuleScanner.PM_OBJECT);
        }
        traceConsume();
        if (crtToken.tokenId == SptRuleScanner.PM_USER) {
            ua = true; 	   // it's a user attribute
        } else ua = false; // it's an object attribute

        crtToken = myScanner.nextToken();
        if (crtToken.tokenId != SptRuleScanner.PM_ATTR) {
            traceExit("what");
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_ATTR);
        }
        traceConsume();
        crtToken = myScanner.nextToken();

        if (crtToken.tokenId != SptRuleScanner.PM_WORD) {
            traceExit("accessRight");
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_WORD);
        }
        traceConsume();
        rule1_attr = crtToken.tokenValue;
        crtToken = myScanner.nextToken();

        if (crtToken.tokenId == SptRuleScanner.PM_IN) {
            traceConsume();
            crtToken = myScanner.nextToken();
            if (crtToken.tokenId != SptRuleScanner.PM_WORD) {
                traceExit("what");
                return signalError(crtToken.tokenValue, SptRuleScanner.PM_WORD);
            }
            traceConsume();
            rule1_attrIn = crtToken.tokenValue;
        }
        if (ua) semopUA();
        else semopOA();
        crtToken = myScanner.nextToken();
        return null;
    }

    public void semopUA() throws Exception {
        Node parent = null;
        Node ua = null;
        int i=0;
        ua = g.createNode(nodeid++, rule1_attr, NodeType.UA, null);
        associationTargets.add(ua);
        if (rule1_attrIn != null) {
            parent = g.createNode(nodeid++, rule1_attrIn, NodeType.UA, null);
            g.assign(ua.getID(), parent.getID());
        }
    }

    public void semopOA() throws Exception {
        Node parent = null;
        int i = 0;
        Node oa = g.createNode(nodeid++, rule1_attr, NodeType.OA, null);
        associationTargets.add(oa);
        if (rule1_attrIn != null) {
            parent = g.createNode(nodeid++, rule1_attrIn, NodeType.OA, null);
            g.assign(oa.getID(), parent.getID());
        }
    }

    // <when clause> ::= when user is ua_name [in ua_name] in policy pc_name
    private String whenClause1() throws Exception {
        String result = null;
        traceEntry("whenClause1");

        if (crtToken.tokenId != SptRuleScanner.PM_WHEN) {
            traceExit("whenClause1");
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_WHEN);
        }
        traceConsume();
        crtToken = myScanner.nextToken();

        if (crtToken.tokenId != SptRuleScanner.PM_USER) {
            traceExit("whenClause1");
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_USER);
        }
        traceConsume();
        crtToken = myScanner.nextToken();

        if (crtToken.tokenId != SptRuleScanner.PM_IS) {
            traceExit("whenClause1");
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_IS);
        }
        traceConsume();
        crtToken = myScanner.nextToken();

        if (crtToken.tokenId != SptRuleScanner.PM_WORD) {
            traceExit("whenClause1");
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_WORD);
        }
        traceConsume();
        int i=0;

        uaWhenElements.add(i++, crtToken.tokenValue);
        crtToken = myScanner.nextToken();
        if (crtToken.tokenId != SptRuleScanner.PM_IN) {
            traceExit("whenClause1");
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_IN);
        }
        traceConsume();
        crtToken = myScanner.nextToken();
        while (crtToken.tokenId != SptRuleScanner.PM_POLICY ) {
            if (crtToken.tokenId != SptRuleScanner.PM_WORD) {
                traceExit("whenClause1");
                return signalError(crtToken.tokenValue, SptRuleScanner.PM_WORD);
            }
            traceConsume();
            uaWhenElements.add(i++, crtToken.tokenValue);
            crtToken = myScanner.nextToken();

            if (crtToken.tokenId != SptRuleScanner.PM_IN) {
                traceExit("whenClause1");
                return signalError(crtToken.tokenValue, SptRuleScanner.PM_IN);
            }
            traceConsume();
            crtToken = myScanner.nextToken();
        }
        if (crtToken.tokenId != SptRuleScanner.PM_POLICY) {
            traceExit("whenClause1");
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_POLICY);
        }
        traceConsume();
        crtToken = myScanner.nextToken();

        if (crtToken.tokenId != SptRuleScanner.PM_WORD) {
            traceExit("whenClause1");
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_WORD);
        }
        traceConsume();
        uaWhenElements.add(i++, crtToken.tokenValue);
        semopUaInPC();
        crtToken = myScanner.nextToken();
        return result;
    }

    public void semopUaInPC() throws Exception {
        String uaName="";
        Node ua;
        int count = uaWhenElements.size();
        rule1_pc = uaWhenElements.get(count-1);
        if (g== null ) {
            System.out.println("Graph is null");
        }
        Node parent = g.createNode(nodeid++,rule1_pc, NodeType.PC,null);
        for(int i=count-2; i >= 0 ;i--) {
            uaName = uaWhenElements.get(i);
            ua = g.createNode(nodeid++,uaName, NodeType.UA,null);
            g.assign(ua.getID(), parent.getID());
            parent = ua;
        }
        associationSource = parent;
    }


    /////////////////// SPT semantic operator Methods
    private void semopRule1Init() {
        traceSemop("semopRule1Init");
        g = SingletonGraph.getInstance();
    }
}
