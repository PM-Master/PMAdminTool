package gov.nist.csd.pm.admintool.spt.parser;

import gov.nist.csd.pm.admintool.graph.SingletonGraph;
import gov.nist.csd.pm.admintool.spt.common.PMElement;
import gov.nist.csd.pm.admintool.spt.common.RandomGUID;
import gov.nist.csd.pm.admintool.spt.common.SptToken;
import gov.nist.csd.pm.graph.GraphSerializer;
import gov.nist.csd.pm.graph.model.nodes.NodeType;
import gov.nist.csd.pm.graph.model.nodes.Node;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SptRuleParser {

	///////////// SPT variables
	
    SptToken crtToken;
    SptRuleScanner myScanner;
    String scriptName;
    String scriptId;
    String rule1_pc = "";
    String rule1_ua = "";
	String rule1_attr = "";
	String rule1_attrIn = "";
	String uattr = "";
	String uattrContainer = "";
	String filename = "PMcmds-"+generateRandomIntegerId().toString();
	FileOutputStream out = null;
	ArrayList<String> uaWhenElements = new ArrayList<String>();
	String parent;
	String parentType;
    SingletonGraph g; // = SingletonGraph.getInstance();

    Node associationSource;
    ArrayList<Node> associationTargets = new ArrayList<Node>();
    Set<String> associationOperations = new HashSet<>();

    public static Long nodeid = 1L;
    ///////////////////// SPT methods ////////////////////////////

    public static void main(String args[]) {
    	
        if (args.length < 1) {
            System.out.println("Missing argument: input file!");
            System.exit(-1);
        }
        try {
        	SptRuleParser ruleParser = new SptRuleParser(args[0]);
            ruleParser.printTokens();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SptRuleParser(String script) {
        myScanner = new SptRuleScanner(script);
        
    }

    public SptRuleParser(File inputFile) {
        myScanner = new SptRuleScanner(inputFile);
    }

    /**
	 * @return
	 * @uml.property  name="scriptName"
	 */
    public String getScriptName() {
        return scriptName;
    }

    /**
	 * @return
	 * @uml.property  name="scriptId"
	 */
    public String getScriptId() {
        return scriptId;
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
//    	out = new FileOutputStream("c://pm//conf//commands"+filename+".pm");

        traceEntry("script");

        result = scriptHeader();
        if (result != null) {
            traceExit("script");
            return result;
        }

        result = rules();
//    	out.close();
        String json = GraphSerializer.toJson(g);

        traceExit("script");
        return json;
    }

    // <script header> ::= script script_name
    private String scriptHeader() {
        traceEntry("scriptHeader");

        if (crtToken.tokenId != SptRuleScanner.PM_SCRIPT) {
            traceExit("scriptHeader");
            return signalError(crtToken.tokenValue, SptRuleScanner.PM_SCRIPT);
        }
        traceConsume();
        crtToken = myScanner.nextToken();
        if (crtToken.tokenId != SptRuleScanner.PM_WORD) {
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
        while (crtToken.tokenId != SptRuleScanner.PM_EOF) {
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
        
        while (crtToken.tokenId != SptRuleScanner.PM_EOF) {
        	System.out.println("Id is " + crtToken.tokenId + " and value is " + crtToken.tokenValue);
        	crtToken = myScanner.nextToken();
        }
    }
    
    // Note that we allow "script name" and rules to be interspersed. This allows
    // concatenation of multiple scripts without having to deal with header
    // deletion. All interior script headers are ignored.
    
    // <rule> ::=  <rule 1> | <rule 2> | <rule 3> 
    private String rule() throws Exception {
    	String result = null;
        traceEntry("rule");

        if (crtToken.tokenId == SptRuleScanner.PM_RULE1) {
            result = rule1();
        } else if (crtToken.tokenId == SptRuleScanner.PM_RULE2) {
        	result = rule2();
        } else if (crtToken.tokenId == SptRuleScanner.PM_RULE3) {
        	result = rule3();
		} else return signalError(crtToken.tokenValue, SptRuleScanner.PM_RULE1);

        return result;
    }

    // <rule1> ::= rule1 <allow clause> <when clause>
    private String rule1() throws Exception {
    	String result = null;
        traceEntry("rule1");

        if (crtToken.tokenId == SptRuleScanner.PM_RULE1) {
            traceConsume();
            crtToken = myScanner.nextToken();
            semopRule1Init();
            result = whenClause1();
            if (result != null) {
            	System.out.println("*********************Exiting due to error **************************" + result);
        		return result;
        	}

            result = allowClause1();

            if (result != null) {
            	System.out.println("*********************Exiting due to error **************************" + result);
        		return result;
        	}
            return result;
        } else return signalError(crtToken.tokenValue, SptRuleScanner.PM_RULE1);
    }

    // <rule2> ::= rule2 <allow clause> <when clause>
    private String rule2() throws Exception {
    	String result = null;
        traceEntry("rule2");

        if (crtToken.tokenId == SptRuleScanner.PM_RULE2) {
            traceConsume();
            crtToken = myScanner.nextToken();
            result = allowClause1();
            if (result != null) {
        		return result;
        	}
            result = whenClause1();
     		return result;
        } else return signalError(crtToken.tokenValue, SptRuleScanner.PM_RULE2);
    }         

    // <rule3> ::= rule3 <allow clause> <when clause>
    private String rule3() throws Exception {
    	String result = null;
        traceEntry("rule3");

        if (crtToken.tokenId == SptRuleScanner.PM_RULE3) {
            traceConsume();
            crtToken = myScanner.nextToken();
            result = allowClause1();
            if (result != null) {
        		return result;
        	}
            result = whenClause1();
            // create associations between associationSource and associationTargets
            for (Node target : associationTargets) {
                g.associate(associationSource.getID(),target.getID(),associationOperations);
            }
     		return result;
        } else return signalError(crtToken.tokenValue, SptRuleScanner.PM_RULE3);
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

    // Utility methods //////////////////////////////////////////////
    
    private Integer generateRandomIntegerId() {
        RandomGUID myGUID = new RandomGUID();
        return myGUID.hashCode();
    }
    
    ///////////////////// end of SPT methods ////////////////////////////

    /////////////////////////////////////////////////////////////////////////////
    ////////////////////////// Utility Methods //////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////
    private String signalError(String found, int expected) {
        return "2 Error around line " + myScanner.lineno() + ": token \""
                + SptRuleScanner.getTokenValue(expected) + "\" expected. Found \"" + found + "\"!";
    }

    private String signalError(String found, int expected, int expected2) {
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

    private String signalError(String found, int expected, int expected2, int expected3,
                               int expected4) {
        return "5 Error around line " + myScanner.lineno() + ": token \""
                + SptRuleScanner.getTokenValue(expected) + "\" or \""
                + SptRuleScanner.getTokenValue(expected2) + "\" or \""
                + SptRuleScanner.getTokenValue(expected3) + "\" or \""
                + SptRuleScanner.getTokenValue(expected4) + "\" expected. Found \"" + found + "\"!";
    }
    /*
    private String signalError(String found, int expected, int expected2, int expected3,
    int expected4, int expected5) {
    return "Error around line " + myScanner.lineno() + ": token \"" +
    SptRuleScanner.getTokenValue(expected) + "\" or \"" +
    SptRuleScanner.getTokenValue(expected2) + "\" or \"" +
    SptRuleScanner.getTokenValue(expected3) + "\" or \"" +
    SptRuleScanner.getTokenValue(expected4) + "\" or \"" +
    SptRuleScanner.getTokenValue(expected5) + "\" expected. Found \"" + found + "\"!";
    }
     */

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

    private void traceConsume() {
        for (int i = 0; i < depth + depth + 2; i++) {
            System.out.print(' ');
        }
        System.out.println(crtToken.tokenValue + ", " + crtToken.tokenId);
    }

    private void traceEntry(String nonterm) {
        for (int i = 0; i < depth + depth; i++) {
            System.out.print(' ');
        }
        System.out.println("< " + nonterm);
        depth++;
    }

    private void traceExit(String nonterm) {
        depth--;
        for (int i = 0; i < depth + depth; i++) {
            System.out.print(' ');
        }
        System.out.println(">");
    }

    private void traceSemop(String semOp) {
        System.out.println("                                    Semantic operator: " + semOp);
    }

    private void traceSemact(String semAct) {
        System.out.println("                                    - Semantic action: " + semAct);
    }

}
