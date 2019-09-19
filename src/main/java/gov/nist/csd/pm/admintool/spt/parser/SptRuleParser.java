package gov.nist.csd.pm.admintool.spt.parser;

import gov.nist.csd.pm.admintool.spt.common.SptToken;
import java.util.Random;

public class SptRuleParser{

    static SptToken crtToken;
    static SptRuleScanner myScanner;
    public SptRuleParser() {
    }

    public static void main(String args[]) {
    	
        try {
            String rule = "allow teller: rbac,dac; ask ua value: branch\n" +
                    "        to \"Create Object\", \"Delete Object\" \n" +
                    "        in accounts: rbac,dac; ask oa value: branch\n" +
                    "when ua_value = oa_value\n";
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
        System.out.println("=======================================================================================");
        while (crtToken.tokenId != SptRuleScanner.PM_EOF) {
        	System.out.println("Id is " + crtToken.tokenId + " and value is " + crtToken.tokenValue);
        	crtToken = myScanner.nextToken();
        }
        System.out.println("=======================================================================================");
    }
    
    // Note that we allow "script name" and rules to be interspersed. This allows
    // concatenation of multiple scripts without having to deal with header
    // deletion. All interior script headers are ignored.

    private String rule() throws Exception {
    	String result = null;
        traceEntry("rule");

        if (crtToken.tokenId == SptRuleScanner.PM_RULE1) {
            Rule1Parser parser = new Rule1Parser();
            result = parser.rule1();
        } else if (crtToken.tokenId == SptRuleScanner.PM_RULE2) {
            synchronized(this) {
                Rule2Parser parser = new Rule2Parser();
                parser.rule2();
            }
        } else if (crtToken.tokenId == SptRuleScanner.PM_RULE3) {
//        	result = rule3();
		} else return signalError(crtToken.tokenValue, SptRuleScanner.PM_RULE);
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
        System.out.println(crtToken.tokenValue + ", " + crtToken.tokenId);
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

}
