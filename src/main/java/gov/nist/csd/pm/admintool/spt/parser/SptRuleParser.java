package gov.nist.csd.pm.admintool.spt.parser;

import gov.nist.csd.pm.admintool.spt.common.PMElement;
import gov.nist.csd.pm.admintool.spt.common.PM_NODE;
import gov.nist.csd.pm.admintool.spt.common.RandomGUID;
import gov.nist.csd.pm.admintool.spt.common.SptToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static gov.nist.csd.pm.admintool.spt.common.GlobalConstants.*;

/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:02:58 $
 * @since 1.5
 */
public class SptRuleParser {

	
	///////////// SPT variables
	
	public ArrayList<PMElement> allowElementsArray;
	public ArrayList<PMElement> whenElementsArray;
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

    public SptRuleParser(String path) {
        myScanner = new SptRuleScanner(path);
        
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
//            if (ServerConfig.myEngine != null) {
           System.out.println(result);
               // ServerConfig.myEngine.deleteScriptInternal(scriptId);
//            }
        }
        return result;
    }

    // <script> ::= <script header> <rules>
    private String script() throws Exception {
        String result = null;
    	out = new FileOutputStream("c://pm//conf//commands"+filename+".pm");

        traceEntry("script");

        result = scriptHeader();
        if (result != null) {
            traceExit("script");
            return result;
        }

        result = rules();
    	out.close();                 

        traceExit("script");
        return result;
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
//        semopScript();
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
            result = whenClause();
            if (result != null) {
            	System.out.println("*********************Exiting due to error **************************" + result);
        		return result;
        	}

            result = allowClause();

            if (result != null) {
            	System.out.println("*********************Exiting due to error **************************" + result);
        		return result;
        	}
        	printPMElements();
        	semopGeneratePM();
     		return result;
        } else return signalError(crtToken.tokenValue, SptRuleScanner.PM_RULE1);
    }

    
    public String semopGeneratePM() throws IOException {
    	// generate PM commands based on allowPMElements and whenPMElements. 
    	// Create nodes, create assignments, create associations in correct order!
    	String ngacCommands = "";
    	System.out.println("Generating whenElements NGACCommands");
        String type = "";
        ArrayList<String> in = new ArrayList<String>();
    	String[][] whats = new String[10][2];
    	for(PMElement pmEl: whenElementsArray){
    		type = pmEl.getType();
            if (type.equals("p")) {
           		ngacCommands += "add|"+type+"|"+pmEl.getName()+"|c|PM"+"\r\n";
           		break;
    		}
    	}
    	String inValue;
    	for(PMElement pmEl: whenElementsArray){
            inValue = null;
    		type = pmEl.getType();
            if (!type.equals("p")) {
	            in = pmEl.getIn();
	            if (in != null) {
	            	inValue = in.get(0);
	            	rule1_ua = pmEl.getName();
	            	ngacCommands += "add|"+type+"|"+pmEl.getName()+"|a|"+inValue+"\r\n";
	            } else {
	            	ngacCommands += "add|"+type+"|"+pmEl.getName()+"|p|"+rule1_pc+"\r\n";
	            }
            }
        }

    	System.out.println("Generating allowElements NGACCommands");
    	// get first elements with no parent (these are directly under policy)
    	for(PMElement pmEl: allowElementsArray){
        	type = pmEl.getType();
            
            if (!type.equals("op")) { 
	    		in = pmEl.getIn();
	    		if (in == null || in.size() == 0) {
	       			parent =  rule1_pc;
	        		ngacCommands += "add|"+type+"|"+pmEl.getName()+"|p|"+parent+"\r\n";
	    		}
            }
    	}
    	// get elements with parent (these are under other elements)
    	int i = 0;
    	for(PMElement pmEl: allowElementsArray){
        	type = pmEl.getType();
            if (!type.equals("op")) { 
	    		in = pmEl.getIn();
	    		if (in != null && in.size() > 0) {
	    			parent = pmEl.getIn().get(0);
	    			whats[i][0] = pmEl.getName();
	    			whats[i][1] = type;
	        		ngacCommands += "add|"+type+"|"+pmEl.getName()+"|"+getParentType(type)+"|"+parent+"\r\n";
	    		}
            }
    	}
    	String rule1_opset = generateRandomIntegerId().toString();
    	
    	// create and assign opset to all the whats
    	ngacCommands += "add|s|"+rule1_opset+"|oc|Ignored|"+whats[0][1]+"|"+whats[0][0]+"\r\n";
    	i = 0;
    	for(String[] what: whats){
    		if (i==0) continue; 
    		if (what[0]!=null) {
    			ngacCommands += "asg|s|"+rule1_opset+"|"+what[1]+"|"+what[0]+"\r\n";
    		}
    	}
    	// add ops under opset
    	for(PMElement pmEl: allowElementsArray){
        	type = pmEl.getType();
            if (type.equals("op")) {
            	// add op to opset
            	ngacCommands += "add|op|"+pmEl.getName()+"|s|"+rule1_opset+"\r\n";
            }
    	}
    	
    	ngacCommands += "asg|a|" + rule1_ua+ "|s|"+rule1_opset+"\r\n";
    	// assign uattr to opset
    	out.write(ngacCommands.getBytes());
        return ngacCommands;
    }

    private String getParentType(String type) {
    	String returnString = "";
    	switch (type){
			case "u": returnString = "a";
			case "a": returnString = "a";
			case "o": returnString = "b";
			case "b": returnString = "b";
			case "p": returnString = "c";
			case "s": returnString = "b";
    	}
    	return returnString;
    }
    
    // <rule2> ::= rule2 <allow clause> <when clause>
    private String rule2() throws Exception {
    	String result = null;
        traceEntry("rule2");

        if (crtToken.tokenId == SptRuleScanner.PM_RULE2) {
            traceConsume();
            crtToken = myScanner.nextToken();
            result = allowClause();
            if (result != null) {
        		return result;
        	}
            result = whenClause();
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
            result = allowClause();
            if (result != null) {
        		return result;
        	}
            result = whenClause();
     		return result;
        } else return signalError(crtToken.tokenValue, SptRuleScanner.PM_RULE3);
    }  
    
    // <allow clause> := allow user <access rights> <on Whats> 
    private String allowClause() throws Exception {
    	 String result=null;
    	 traceEntry("allowClause");
    	 if (crtToken.tokenId != SptRuleScanner.PM_ALLOW) {
    		 traceExit("allowClause");
    		 return signalError(crtToken.tokenValue, SptRuleScanner.PM_ALLOW); 
    	 }
    	 traceConsume();
    	 crtToken = myScanner.nextToken();
    	 // user - commented out for now
    	 if (crtToken.tokenId != SptRuleScanner.PM_USER) {
    		 traceExit("allowClause");
    		 return signalError(crtToken.tokenValue, SptRuleScanner.PM_USER); 
    	 }
    	 traceConsume();
    	 crtToken = myScanner.nextToken();
    	 
    	 result = accessRights();
         if (result != null) {
             traceExit("allowClause");
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
    // int id, String name, String type, PMElement[] in
    private void semopAnAR() throws Exception{
        String id="";
    	System.out.println("About to find node " + crtToken.tokenValue + " in database as type " + PM_NODE.OPERATION.value);
        if (crtToken.tokenValue.equalsIgnoreCase(PM_FILE_READ)) {
            id = PM_FILE_READ_VAL;
        } else if (crtToken.tokenValue.equalsIgnoreCase(PM_FILE_WRITE)) {
            id = PM_FILE_WRITE_VAL;
        }
    	Integer elementId = new Integer(id);
    	PMElement pmElement = new PMElement(elementId, crtToken.tokenValue, PM_NODE.OPERATION.value, null);
    	allowElementsArray.add(pmElement);
    	
    }
    
    private void printPMElements() {
		System.out.println(" ************* LIST of allow Elements *********** ");
		
        for(PMElement pmEl: allowElementsArray){
            System.out.println("   id : " + pmEl.getId());
    		System.out.println(" Name : " + pmEl.getName());
    		System.out.println(" Type : " + pmEl.getType());
    		ArrayList<String> parents = pmEl.getIn();
    		if (parents == null) {
        		System.out.println("   in : null");
    		} else {
        		System.out.println("   in : ");
	    		for (int i = 0; i < parents.size(); i++) {
	    			System.out.println(" id : " + parents.get(i));
	    		}
    		}
        }

		System.out.println(" ************* LIST of when Elements *********** " + whenElementsArray.size());

        for(PMElement pmEl: whenElementsArray){
            System.out.println("   id : " + pmEl.getId());
    		System.out.println(" Name : " + pmEl.getName());
    		System.out.println(" Type : " + pmEl.getType());
    		ArrayList<String> parents = pmEl.getIn();
    		if (parents == null) {
        		System.out.println("   in : null");
    		} else {
        		System.out.println("   in : ");
	    		for (int i = 0; i < parents.size(); i++) {
	    			System.out.println(" id : " + parents.get(i));
	    		}
    		}
        }

        System.out.println(" ************* End of PMElements *********** ");    		
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
	   		result = what();
	   		if (result != null) {
                traceExit("onWhats");
                return result;
            }
            if (crtToken.tokenId != SptRuleScanner.PM_COMMA) {
                break;
            }
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
    	ArrayList<String> in = null;
    	if (rule1_attrIn != null) {
    		PMElement pmparent = new PMElement((Integer) null, rule1_attrIn, PM_NODE.UATTR.value, null);
        	allowElementsArray.add(pmparent);
    		in = new ArrayList<String>();
        	in.add(0, rule1_attrIn);
    	}
    	PMElement pmElement = new PMElement((Integer) null, rule1_attr, PM_NODE.UATTR.value, in);
    	allowElementsArray.add(pmElement);
    }
    
    public void semopOA() throws Exception {
    	ArrayList<String> in = null;
    	if (rule1_attrIn != null) { 
	    	PMElement pmparent = new PMElement((Integer) null, rule1_attrIn, PM_NODE.OATTR.value, null);
	    	allowElementsArray.add(pmparent);
	    	in = new ArrayList<String>();
	    	in.add(0, rule1_attrIn);
    	}
    	PMElement pmElement = new PMElement((Integer) null, rule1_attr, PM_NODE.OATTR.value, in);
    	allowElementsArray.add(pmElement);
    }
        
    // <when clause> ::= when user is ua_name [in ua_name] in policy pc_name
    private String whenClause() throws Exception {
    	String result = null;
    	traceEntry("whenClause");
    	
        if (crtToken.tokenId != SptRuleScanner.PM_WHEN) {
   	 		traceExit("whenClause");
   	 		return signalError(crtToken.tokenValue, SptRuleScanner.PM_WHEN); 
   	 	}
        traceConsume();
        crtToken = myScanner.nextToken();
        
        if (crtToken.tokenId != SptRuleScanner.PM_USER) {
   	 		traceExit("whenClause");
   	 		return signalError(crtToken.tokenValue, SptRuleScanner.PM_USER); 
   	 	}
        traceConsume();
        crtToken = myScanner.nextToken();
        
        if (crtToken.tokenId != SptRuleScanner.PM_IS) {
   	 		traceExit("whenClause");
   	 		return signalError(crtToken.tokenValue, SptRuleScanner.PM_IS); 
   	 	}
        traceConsume();
        crtToken = myScanner.nextToken();
        
        if (crtToken.tokenId != SptRuleScanner.PM_WORD) {
   	 		traceExit("whenClause");
   	 		return signalError(crtToken.tokenValue, SptRuleScanner.PM_WORD); 
   	 	}
        traceConsume();
        int i=0;
        uaWhenElements.add(i++, crtToken.tokenValue);
        crtToken = myScanner.nextToken();
        if (crtToken.tokenId != SptRuleScanner.PM_IN) {
   	 		traceExit("whenClause");
   	 		return signalError(crtToken.tokenValue, SptRuleScanner.PM_IN); 
   	 	}
        traceConsume();
        crtToken = myScanner.nextToken();
        while (crtToken.tokenId != SptRuleScanner.PM_POLICY ) {
        	if (crtToken.tokenId != SptRuleScanner.PM_WORD) {
       	 		traceExit("whenClause");
       	 		return signalError(crtToken.tokenValue, SptRuleScanner.PM_WORD); 
       	 	} 
        	traceConsume();
        	uaWhenElements.add(i++, crtToken.tokenValue);
            crtToken = myScanner.nextToken();
            
            if (crtToken.tokenId != SptRuleScanner.PM_IN) {
       	 		traceExit("whenClause");
       	 		return signalError(crtToken.tokenValue, SptRuleScanner.PM_IN); 
       	 	}
            traceConsume();
            crtToken = myScanner.nextToken();                
        }
        if (crtToken.tokenId != SptRuleScanner.PM_POLICY) {
   	 		traceExit("whenClause");
   	 		return signalError(crtToken.tokenValue, SptRuleScanner.PM_POLICY); 
   	 	}
        traceConsume();
        crtToken = myScanner.nextToken();
        
        if (crtToken.tokenId != SptRuleScanner.PM_WORD) {
   	 		traceExit("whenClause");
   	 		return signalError(crtToken.tokenValue, SptRuleScanner.PM_WORD); 
   	 	}
        traceConsume();
        uaWhenElements.add(i++, crtToken.tokenValue);
        printUaWhenElements();
        semopUaInPC();
        crtToken = myScanner.nextToken();
        
        return result;
    }

    void printUaWhenElements() {
    	int count = uaWhenElements.size();
    	System.out.println(" ********** uaWhenElements ****** size = "+ count);
    	for(int i=0;i<count;i++) {
    		System.out.println(" element " + i + " = " + uaWhenElements.get(i));
    	}
    }
    
    
    public void semopUaInPC() throws Exception {
    	String element="";
    	String policy;
    	PMElement pmElement;
    	PMElement pmparent;
    	ArrayList<String> in = null;
    	int count = uaWhenElements.size();
    	policy = uaWhenElements.get(count-1);
    	rule1_pc = policy;
    	pmparent = new PMElement((Integer) null, policy, PM_NODE.POL.value, null);
    	whenElementsArray.add(pmparent);
    	for(int i=count-2; i >= 0 ;i--) {
    		element = uaWhenElements.get(i);
    		pmElement = new PMElement((Integer) null, element, PM_NODE.UATTR.value, in);
	    	whenElementsArray.add(pmElement);
	    	in = new ArrayList<String>();
	    	in.add(0, element);
    	}
	}


    /////////////////// SPT semantic operator Methods
    private void semopRule1Init() {
        traceSemop("semopRule1Init");

        traceSemact("create the ruleElements with an empty array of elements");
        allowElementsArray = new ArrayList<PMElement>();
        whenElementsArray = new ArrayList<PMElement>();
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
