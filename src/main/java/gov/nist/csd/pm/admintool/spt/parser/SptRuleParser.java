package gov.nist.csd.pm.admintool.spt.parser;

import gov.nist.csd.pm.admintool.graph.SingletonClient;
import gov.nist.csd.pm.admintool.spt.common.SptToken;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.pdp.audit.model.Explain;
import gov.nist.csd.pm.pdp.audit.model.Path;
import gov.nist.csd.pm.pdp.audit.model.PolicyClass;
import gov.nist.csd.pm.pdp.services.UserContext;
import gov.nist.csd.pm.pip.graph.model.nodes.Node;

import java.util.*;

public class SptRuleParser{

    static SptToken crtToken;
    static SptRuleScanner myScanner;
    private static Random rand;
    public SptRuleParser() {
    }

    public static void main(String[] args) {
    	
        try {
            String rule = "rule1 \n" +
                    "allow teller->staff: rbac\n" +
                    "        to \"read\", \"write\" \n" +
                    "        in branchAccounts->accounts\n" +
                    "\n" +
                    "rule2 \n" +
                    "allow teller->staff: rbac; ask ua value: branch\n" +
                    "        to \"read\", \"write\" \n" +
                    "        in branchAccounts->accounts: rbac; ask oa value: branch\n" +
                    "when ua_value = oa_value";
        	SptRuleParser ruleParser = new SptRuleParser(rule);
            ruleParser.printTokens();

            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SptRuleParser(String script) {
        myScanner = new SptRuleScanner(script);
        
    }

    // Invoke the function corresponding to the start nonterminal, script.
    public String parse() throws Exception {
        String result = null;
        crtToken = myScanner.nextToken();
        result = script();
        if (result != null) {
           System.out.println(result);
        }
        return result;
    }

    // <script> ::= <script header> <rules>
    private String script() throws Exception {
        String result = null;
        if (SptToken.tokenId != SptRuleScanner.PM_SCRIPT) {
            traceExit("scriptHeader");
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_SCRIPT);
        }

        traceEntry("script");

        result = scriptHeader();
        if (result != null) {
            traceExit("script");
            return result;
        }

        result = rules();
        traceExit("script");

        return result;
    }

    // <script header> ::= script script_name
    private String scriptHeader() {
        traceEntry("scriptHeader");

        traceConsume();
        crtToken = myScanner.nextToken();
        if (SptToken.tokenId != SptRuleScanner.PM_WORD) {
            traceExit("scriptHeader");
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_WORD);
        }
        traceConsume();
        crtToken = myScanner.nextToken();

        traceExit("scriptHeader");
        return null;
    }
    // <rules> ::= { <rule> }
    private String rules() throws Exception {
        String result = null;

        traceEntry("rules");
        while (SptToken.tokenId != SptRuleScanner.PM_EOF) {
            result = rule();
            if (result != null) {
                traceExit("rules");
                return result;
            }
        }
        traceExit("rules");
        return null;
    }

    
    public void printTokens() throws Exception {
        crtToken = myScanner.nextToken();
        System.out.println("=======================================================================================");
        while (SptToken.tokenId != SptRuleScanner.PM_EOF) {
        	System.out.println("Id is " + SptToken.tokenId + " and value is " + crtToken.tokenValue);
        	crtToken = myScanner.nextToken();
        }
        System.out.println("=======================================================================================");
    }
    
    // Note that we allow "script name" and rules to be interspersed. This allows
    // concatenation of multiple scripts without having to deal with header
    // deletion. All interior script headers are ignored.

    private String rule() throws Exception {
    	String result = null;
        ArrayList<Rule1Parser> rule1Parsers = new ArrayList<Rule1Parser>();
        ArrayList<Rule2Parser> rule2Parsers = new ArrayList<Rule2Parser>();;

        Rule1Parser parser1 = null;
        Rule2Parser parser2 = null;

        traceEntry("rule");
        while (SptToken.tokenId == SptRuleScanner.PM_RULE1 || SptToken.tokenId == SptRuleScanner.PM_RULE2){
            if (SptToken.tokenId == SptRuleScanner.PM_RULE1) {
                parser1 = new Rule1Parser();
                result = parser1.rule1();
                rule1Parsers.add(parser1);
            } else if (SptToken.tokenId == SptRuleScanner.PM_RULE2) {
                synchronized (this) {
                    parser2 = new Rule2Parser();
                    parser2.rule2();
                    rule2Parsers.add(parser2);
                }
            } else return signalError(crtToken.tokenValue, SptRuleScanner.PM_RULE);
        }
        /* Test
        * 1. loop through each rule in script
        * 2. loop through each triplet in purpose of the current rule
        * 3. add/assign a dummy user to the UA and add/assign a dummy object to the OA
        * 4. Test if the purpose is solved
        * 5. If not, call Analyse() to see the paths and equate it with purpose,
        *    then make smart decision to fix it so that purpose
        *    and go back to the first rule again to test from beginning
        * 6. Once the current rule purpose succeed, move on to the next rule.*/
/* This test can be done using the tester, so commenting out
        for (Rule1Parser rp: rule1Parsers) {
            // TODO
        }
        for (Rule2Parser rp: rule2Parsers) {
            // add a dummy user
            Node dummyUser = rp.graph.createNode("dummyUA", NodeType.UA,null, rp.purpose.get(0).fromNode.getName());
            // add a dummy object
            Node dummyObject = rp.graph.createNode("dummyOA", NodeType.OA,null,rp.purpose.get(0).toNode.getName());
            for (Rule2Parser.Purpose p:rp.purpose) {
                // assign the dummy user to p.fromNode
                rp.graph.assign(dummyUser.getName(), p.fromNode.getName());
                // assign the dummy user to p.toNode
                rp.graph.assign(dummyObject.getName(), p.toNode.getName());
                // Check permissions for (dummyUser, ,dummyObject
                if (dummyUser != null && dummyObject != null && rp.associationOperations != null) {
                    SingletonClient g = SingletonClient.getInstance();
                    try {
                        Set<String> perms = g.getAnalyticsService(new UserContext(dummyUser.getName(),rand.toString() )).getPermissions(dummyObject.getName());
                        if (!perms.containsAll(rp.associationOperations)) {
                            rp.analyze(p);
                            perms = g.getAnalyticsService(new UserContext(dummyUser.getName(), rand.toString())).getPermissions(dummyObject.getName());
                            if (!perms.containsAll(rp.associationOperations)) {
                                // return notifying "The policy can not be impmented for an unknown reason. Return all the paths.
                            }

                        }
                    } catch (PMException e) {
                        e.printStackTrace();
                        System.out.println(e.getMessage());
                    }
                }
            }
        } */
        return result;
    }

    /////////////////////////////////////////////////////////////////////////////
    ////////////////////////// Utility Methods //////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////
    protected Long generateRandomId() {
        Random rand = new Random();
        return rand.nextLong();
    }

    protected String signalError(String found, int expected) {
        return "2 Error around line " + myScanner.lineno() + ": token \""
                + SptRuleScanner.getTokenValue(expected) + "\" expected. Found \"" + found + "\"!";
    }

    protected String signalError(String found, int expected, int expected2) {
        return "3 Error around line " + myScanner.lineno() + ": token \""
                + SptRuleScanner.getTokenValue(expected) + "\" or \""
                + SptRuleScanner.getTokenValue(expected2) + "\" expected. Found \"" + found + "\"!";
    }

    private String signalError(String found, int expected, int expected2, int expected3) {
        return "4 Error around line " + myScanner.lineno() + ": token \""
                + SptRuleScanner.getTokenValue(expected) + "\" or \""
                + SptRuleScanner.getTokenValue(expected2) + "\" or \""
                + SptRuleScanner.getTokenValue(expected3) + "\" expected. Found \"" + found + "\"!";
    }

    protected String signalError(String found, int expected, int expected2, int expected3,
                               int expected4) {
        return "5 Error around line " + myScanner.lineno() + ": token \""
                + SptRuleScanner.getTokenValue(expected) + "\" or \""
                + SptRuleScanner.getTokenValue(expected2) + "\" or \""
                + SptRuleScanner.getTokenValue(expected3) + "\" or \""
                + SptRuleScanner.getTokenValue(expected4) + "\" expected. Found \"" + found + "\"!";
    }

    private String signalError(String found, int expected, int expected2, int expected3,
                               int expected4, int expected5, int expected6) {
        return "Error around line " + myScanner.lineno() + ": token \""
                + SptRuleScanner.getTokenValue(expected) + "\" or \""
                + SptRuleScanner.getTokenValue(expected2) + "\" or \""
                + SptRuleScanner.getTokenValue(expected3) + "\" or \""
                + SptRuleScanner.getTokenValue(expected4) + "\" or \""
                + SptRuleScanner.getTokenValue(expected5) + "\" or \""
                + SptRuleScanner.getTokenValue(expected6) + "\" expected. Found \"" + found + "\"!";
    }
    static int depth = 0;

    protected void traceConsume() {
        for (int i = 0; i < depth + depth + 2; i++) {
            System.out.print(' ');
        }
        System.out.println(crtToken.tokenValue + ", " + SptToken.tokenId);
    }

    protected void traceEntry(String nonterm) {
        for (int i = 0; i < depth + depth; i++) {
            System.out.print(' ');
        }
        System.out.println("< " + nonterm);
        depth++;
    }

    protected void traceExit(String nonterm) {
        depth--;
        for (int i = 0; i < depth + depth; i++) {
            System.out.print(' ');
        }
        System.out.println(">");
    }

    protected void traceSemop(String semOp) {
        System.out.println("                                    Semantic operator: " + semOp);
    }

    private void traceSemact(String semAct) {
        System.out.println("                                    - Semantic action: " + semAct);
    }

    public String explain(Node user, Node target) {
        if (user != null && target != null) {
            SingletonClient g = SingletonClient.getInstance();

            Explain explain = g.explain(new UserContext(user.getName(), rand.toString()), target.getName());

            if (explain != null) {
                String ret = "";
                // Explain returns two things:
                //  1. The permissions the user has on the target
                //  2. A breakdown of permissions per policy class and paths in each policy class
                ret += "'" + user.getName() + "' has the following permissions on the target '" + target.getName() + "': \n";
                Set<String> permissions = explain.getPermissions();
                for (String perm : permissions) {
                    ret += "\t- " + perm + "\n";
                }
                ret += "\n";


                // policyClasses maps the name of a policy class node to a Policy Class object
                // a policy class object contains the permissions the user has on the target node
                //   in that policy class
                ret += "The following section shows a more detailed permission breakdown from the perspective of each policy class:\n";
                Map<String, PolicyClass> policyClasses = explain.getPolicyClasses();
                int i = 1;
                for (String pcName : policyClasses.keySet()) {
                    ret += "\t" + i + ". '" + pcName + "':\n";
                    PolicyClass policyClass = policyClasses.get(pcName);

                    // the operations available to the user on the target under this policy class
                    Set<String> operations = policyClass.getOperations();
                    ret += "\t\t- Permissions (Given by this PC):\n";
                    for (String op : operations) {
                        ret += "\t\t\t- " + op + "\n";
                    }
                    // the paths from the user to the target
                    // A Path object contains the path and the permissions the path provides
                    // the path is just a list of nodes starting at the user and ending at the target node
                    // example: u1 -> ua1 -> oa1 -> o1 [read]
                    //   the association ua1 -> oa1 has the permission [read]
                    ret += "\t\t- Paths (How each permission is found):\n";
                    Set<Path> paths = policyClass.getPaths();
                    for (Path path : paths) {
                        ret += "\t\t\t";
                        // this is just a list of nodes -> [u1, ua1, oa1, o1]
                        List<Node> nodes = path.getNodes();
                        for (Node n : nodes) {
                            ret += "'" + n.getName() + "'";
                            if (!nodes.get(nodes.size() - 1).equals(n)) { // not final node
                                ret += " > ";
                            }
                        }

                        // this is the operations in the association between ua1 and oa1
                        Set<String> pathOps = path.getOperations();
                        ret += " " + pathOps;
                        // This is the string representation of the path (i.e. "u1-ua1-oa1-o1 ops=[r, w]")
                        String pathString = path.toString();
                        ret += "\n";
                    }
                    i++;
                }

                return ret;
            } else {
                return "Returned Audit was null";
            }
        } else {
            return "Either User ID or Target ID are null";
        }
    }
}
