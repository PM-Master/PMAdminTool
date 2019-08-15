package gov.nist.csd.pm.admintool.spt.parser;

import gov.nist.csd.pm.admintool.spt.common.SptToken;

import java.io.*;
import java.util.HashMap;

import java.nio.charset.Charset;
/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:02:58 $
 * @since 1.5
 */
public class SptRuleScanner {

  public static final int PM_IF            = 1;
  public static final int PM_THEN          = 2;
  public static final int PM_ANY           = 3;
  public static final int PM_USER          = 4;
  public static final int PM_OF            = 5;
  public static final int PM_ACTIVE        = 6;
  public static final int PM_IN            = 7;
  public static final int PM_ALL           = 8;
  public static final int PM_PERFORMS      = 9;
  public static final int PM_ON            = 10;
  public static final int PM_CLASS         = 11;
  public static final int PM_ASSIGN        = 12;
  public static final int PM_NEW           = 13;
  public static final int PM_OBJECT        = 14;
  public static final int PM_TO            = 15;
  public static final int PM_LIKE          = 16;
  public static final int PM_CHOICE        = 17;
  public static final int PM_GRANT         = 18;
  public static final int PM_DENY          = 19;
  public static final int PM_OPS           = 20;
  public static final int PM_NAME          = 21;
  public static final int PM_ATTR          = 22;
  public static final int PM_POLICY        = 23;
  public static final int PM_EACH          = 24;
  public static final int PM_OP            = 25;
  public static final int PM_AS            = 26;
  public static final int PM_BASE          = 27;
  public static final int PM_SCRIPT        = 28;
  public static final int PM_CREATE        = 29;
  public static final int PM_WITH          = 30;
  public static final int PM_PROPERTY      = 31;
  public static final int PM_REPR          = 32;
  public static final int PM_AND           = 33;
  public static final int PM_ASCS          = 34;
  public static final int PM_WHEN          = 35;
  public static final int PM_DO            = 36;
  public static final int PM_NOT           = 37;
  public static final int PM_EXISTS        = 38;
  public static final int PM_INTRA         = 39;
  public static final int PM_COMPLEMENT    = 40;
  public static final int PM_INTERSECTION  = 41;
  public static final int PM_ASSIGNMENT    = 42;
  public static final int PM_DELETE        = 43;
  public static final int PM_RULE          = 44;
  public static final int PM_SESSION       = 45;
  public static final int PM_RULES         = 46;
  public static final int PM_RECORD        = 47;
  public static final int PM_PROCESS       = 48;
  public static final int PM_ALLOW         = 49;
  public static final int PM_RULE1         = 50;
  public static final int PM_RULE2         = 51;
  public static final int PM_RULE3         = 52;
  public static final int PM_IS            = 53;
  public static final int PM_ASK            = 54;
  public static final int PM_UA            = 55;
  public static final int PM_VALUE         = 56;
  public static final int PM_OA            = 57;

  public static final int PM_FIRST_KEYWORD = PM_IF;
  public static final int PM_LAST_KEYWORD  = PM_OA;
  

  public static final int PM_WORD          = 100;

  public static final int PM_ARROW         = 200;
  public static final int PM_COMMA         = 201;
  public static final int PM_EQUAL         = 202;
  public static final int PM_LPAR          = 203;
  public static final int PM_RPAR          = 204;
  public static final int PM_COLON         = 205;
  public static final int PM_SEMICOLON     = 206;
  public static final int PM_LESS_THAN      = 207;
  public static final int PM_GREATER_THAN   = 208;
  public static final int PM_LESS_THAN_OR_EQUAL      = 209;
  public static final int PM_GREATER_THAN_OR_EQUAL   = 210;

  public static final int PM_EOF           = 1000;
  public static final int PM_ERROR         = 1001;
  public static final int PM_UNKNOWN       = 1002;

  private static final String PM_VALUE_IF         = "if";
  private static final String PM_VALUE_THEN       = "then";
  private static final String PM_VALUE_ANY        = "any";
  private static final String PM_VALUE_USER       = "user";
  private static final String PM_VALUE_OF         = "of";
  private static final String PM_VALUE_ACTIVE     = "active";
  private static final String PM_VALUE_IN         = "in";
  private static final String PM_VALUE_ALL        = "all";
  private static final String PM_VALUE_PERFORMS   = "performs";
  private static final String PM_VALUE_ON         = "on";
  private static final String PM_VALUE_CLASS      = "class";
  public static final String PM_VALUE_ASSIGN      = "assign";
  private static final String PM_VALUE_NEW        = "new";
  private static final String PM_VALUE_OBJECT     = "object";
  public static final String PM_VALUE_TO          = "to";
  public static final String PM_VALUE_LIKE        = "like";
  private static final String PM_VALUE_CHOICE     = "choice";
  public static final String PM_VALUE_GRANT       = "grant";
  public static final String PM_VALUE_DENY        = "deny";
  private static final String PM_VALUE_OPS        = "operations";
  private static final String PM_VALUE_NAME       = "name";
  private static final String PM_VALUE_ATTR       = "attribute";
  private static final String PM_VALUE_POLICY     = "policy";
  private static final String PM_VALUE_EACH       = "each";
  private static final String PM_VALUE_OP         = "operation";
  private static final String PM_VALUE_AS         = "as";
  private static final String PM_VALUE_BASE       = "base";
  private static final String PM_VALUE_SCRIPT     = "script";
  public static final String PM_VALUE_CREATE      = "create";
  private static final String PM_VALUE_WITH       = "with";
  private static final String PM_VALUE_PROPERTY   = "property";
  private static final String PM_VALUE_REPR       = "representing";
  private static final String PM_VALUE_AND        = "and";
  private static final String PM_VALUE_ASCS       = "ascendants";
  private static final String PM_VALUE_WHEN       = "when";
  private static final String PM_VALUE_DO         = "do";
  private static final String PM_VALUE_NOT        = "not";
  public static final String PM_VALUE_EXISTS      = "exists";
  public static final String PM_VALUE_INTRA       = "intrasession";
  public static final String PM_VALUE_COMPLEMENT  = "complement";
  public static final String PM_VALUE_INTERSECTION= "intersection";
  public static final String PM_VALUE_ASSIGNMENT  = "assignment";
  public static final String PM_VALUE_DELETE      = "delete";
  public static final String PM_VALUE_RULE        = "rule";
  public static final String PM_VALUE_SESSION     = "session";
  public static final String PM_VALUE_RULES       = "rules";
  public static final String PM_VALUE_RECORD      = "record";
  public static final String PM_VALUE_PROCESS     = "process";
  public static final String PM_VALUE_ALLOW       = "allow";
  public static final String PM_VALUE_RULE1       = "rule1";
  public static final String PM_VALUE_RULE2       = "rule2";
  public static final String PM_VALUE_RULE3       = "rule3";
  public static final String PM_VALUE_IS          = "is";
  public static final String PM_VALUE_ASK         = "ask";
  public static final String PM_VALUE_UA          = "ua";
  public static final String PM_VALUE_VALUE       = "value";
  public static final String PM_VALUE_OA          = "oa";

  // RULE 2 new values ***********************

  private static String sKeywords[] =
                      {PM_VALUE_IF, PM_VALUE_THEN, PM_VALUE_ANY,
                       PM_VALUE_USER, PM_VALUE_OF, PM_VALUE_ACTIVE,
                       PM_VALUE_IN, PM_VALUE_ALL, PM_VALUE_PERFORMS,
                       PM_VALUE_ON, PM_VALUE_CLASS, PM_VALUE_ASSIGN,
                       PM_VALUE_NEW, PM_VALUE_OBJECT, PM_VALUE_TO,
                       PM_VALUE_LIKE, PM_VALUE_CHOICE, PM_VALUE_GRANT,
                       PM_VALUE_DENY, PM_VALUE_OPS, PM_VALUE_NAME,
                       PM_VALUE_ATTR, PM_VALUE_POLICY, PM_VALUE_EACH,
                       PM_VALUE_OP, PM_VALUE_AS, PM_VALUE_BASE,
                       PM_VALUE_SCRIPT, PM_VALUE_CREATE, PM_VALUE_WITH,
                       PM_VALUE_PROPERTY, PM_VALUE_REPR, PM_VALUE_AND,
                       PM_VALUE_ASCS, PM_VALUE_WHEN, PM_VALUE_DO,
                       PM_VALUE_NOT, PM_VALUE_EXISTS, PM_VALUE_INTRA,
                       PM_VALUE_COMPLEMENT, PM_VALUE_INTERSECTION,
                       PM_VALUE_ASSIGNMENT, PM_VALUE_DELETE, PM_VALUE_RULE,
                       PM_VALUE_SESSION, PM_VALUE_RULES, PM_VALUE_RECORD,
                       PM_VALUE_PROCESS, PM_VALUE_ALLOW, PM_VALUE_RULE1, 
                       PM_VALUE_RULE2, PM_VALUE_RULE3, PM_VALUE_IS,
                       PM_VALUE_ASK,PM_VALUE_UA,PM_VALUE_VALUE,PM_VALUE_OA
  };

  private static final String PM_VALUE_ARROW         = "->";
  private static final String PM_VALUE_EQUAL         = "=";
  private static final String PM_VALUE_COMMA         = ",";
  private static final String PM_VALUE_LPAR          = "(";
  private static final String PM_VALUE_RPAR          = ")";
  private static final String PM_VALUE_COLON         = ":";


  public static final String PM_VALUE_SEMICOLON     = ";";
  public static final String PM_VALUE_LESS_THAN      = "<";
  public static final String PM_VALUE_GREATER_THAN   = ">";
  public static final String PM_VALUE_LESS_THAN_OR_EQUAL      = "<=";
  public static final String PM_VALUE_GREATER_THAN_OR_EQUAL   = ">=";

  private static final String PM_VALUE_WORD          = "word";

  private static final String PM_VALUE_UNKNOWN       = "unknown";

  /**
 * @uml.property  name="st"
 * @uml.associationEnd  qualifier="sval:java.lang.String java.lang.Integer"
 */
StreamTokenizer st;
  /**
 * @uml.property  name="keywords"
 */
HashMap keywords;
  
//  public SptRuleScanner(String inputFilename) {
//    Reader r = null;
//    try {
//      FileInputStream fis = new FileInputStream(inputFilename);
//      r = new BufferedReader(new InputStreamReader(fis));
//    } catch (Exception e) {
//      e.printStackTrace();
//      System.exit(1);
//    }
//
//    st = new StreamTokenizer(r);
//    st.resetSyntax();
//    st.eolIsSignificant(false);
//    st.lowerCaseMode(false);
//    st.whitespaceChars(0, ' ');
//    st.wordChars('0', '9');
//    st.wordChars('A', 'Z');
//    st.wordChars('a', 'z');
//    st.wordChars('_', '_');
//    st.quoteChar('"');
//    st.slashSlashComments(true);
//
//    keywords = new HashMap();
//    for (int i = 0; i < sKeywords.length; i++) {
//      keywords.put(sKeywords[i], Integer.valueOf(i + PM_FIRST_KEYWORD));
//    }
//  }


  public SptRuleScanner(String script) {
    Reader r = null;
    try {
      InputStream inputStream = new ByteArrayInputStream(script.getBytes(Charset.forName("UTF-8")));
      r = new BufferedReader(new InputStreamReader(inputStream));
    } catch (Exception e) {
      e.printStackTrace();
    }

    st = new StreamTokenizer(r);
    st.resetSyntax();
    st.eolIsSignificant(false);
    st.lowerCaseMode(false);
    st.whitespaceChars(0, ' ');
    st.wordChars('0', '9');
    st.wordChars('A', 'Z');
    st.wordChars('a', 'z');
    st.wordChars('_', '_');
    st.quoteChar('"');
    st.slashSlashComments(true);

    keywords = new HashMap();
    for (int i = 0; i < sKeywords.length; i++) {
      keywords.put(sKeywords[i], Integer.valueOf(i + PM_FIRST_KEYWORD));
    }
  }

  public SptRuleScanner(File inputFile) {
    Reader r = null;
    try {
      FileInputStream fis = new FileInputStream(inputFile);
      r = new BufferedReader(new InputStreamReader(fis));
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
    
    st = new StreamTokenizer(r);
    st.resetSyntax();
    st.eolIsSignificant(false);
    st.lowerCaseMode(false);
    st.whitespaceChars(0, ' ');
    st.wordChars('0', '9');
    st.wordChars('A', 'Z');
    st.wordChars('a', 'z');
    st.wordChars('_', '_');
    st.quoteChar('"');
    st.slashSlashComments(true);
    
    keywords = new HashMap();
    for (int i = 0; i < sKeywords.length; i++) {
      keywords.put(sKeywords[i], Integer.valueOf(i + PM_FIRST_KEYWORD));
    }
  }
  
  public static void printToken(SptToken token) {
    if (token.tokenId >= PM_FIRST_KEYWORD && token.tokenId <= PM_LAST_KEYWORD)
      System.out.println(  "Keyword  : " + token.tokenValue);
    else switch (token.tokenId) {
      case PM_WORD:
        System.out.println("Word     : " + token.tokenValue);
        break;
      case PM_ARROW:
        System.out.println("Arrow    : " + token.tokenValue);
        break;
      case PM_EQUAL:
        System.out.println("Equal    : " + token.tokenValue);
        break;
      case PM_COMMA:
        System.out.println("Comma    : " + token.tokenValue);
        break;
      case PM_LPAR:
        System.out.println("Left Par : " + token.tokenValue);
        break;
      case PM_RPAR:
        System.out.println("Rigth Par: " + token.tokenValue);
        break;
      case PM_COLON:
        System.out.println("Colon: " + token.tokenValue);
        break;
      case PM_UNKNOWN:
        System.out.println("Unknown  : " + token.tokenValue);
        break;
      case PM_ERROR:
        System.out.println("Error  : " + token.tokenValue);
        break;
      case PM_EOF:
        System.out.println("Eof    : " + token.tokenValue);
        break;
      case PM_SEMICOLON:
        System.out.println("Semicolon    : " + token.tokenValue);
        break;
      case PM_LESS_THAN:
        System.out.println("Less than    : " + token.tokenValue);
        break;
      case PM_GREATER_THAN:
        System.out.println("Greater than    : " + token.tokenValue);
        break;
      case PM_LESS_THAN_OR_EQUAL:
        System.out.println("Less than or equal    : " + token.tokenValue);
        break;
      case PM_GREATER_THAN_OR_EQUAL:
        System.out.println("Greater than or equal    : " + token.tokenValue);
        break;
      default:
        System.out.println("Unknown: " + token.tokenValue);
    }
  }

  public SptToken nextToken() {
    int c, d;
    SptToken token;

    try {
      // Get the next token type in c.
      c = st.nextToken();
      if (c == StreamTokenizer.TT_EOF) {
        return new SptToken(PM_EOF, "eof", st.lineno());
      } else if (c == StreamTokenizer.TT_WORD) {
        if (keywords.containsKey(st.sval)) {
          return new SptToken(((Integer)keywords.get(st.sval)).intValue(), st.sval, st.lineno());
        } else {
          return new SptToken(PM_WORD, st.sval, st.lineno());
        }
      } else if (c == '"') {
        if (keywords.containsKey(st.sval)) {
          return new SptToken(((Integer)keywords.get(st.sval)).intValue(), st.sval, st.lineno());
        } else return new SptToken(PM_WORD, st.sval, st.lineno());
      } else if (c == ',') {
        return new SptToken(PM_COMMA, PM_VALUE_COMMA, st.lineno());
      } else if (c == '=') {
        return new SptToken(PM_EQUAL, PM_VALUE_EQUAL, st.lineno());
      } else if (c == '(') {
        return new SptToken(PM_LPAR, PM_VALUE_LPAR, st.lineno());
      } else if (c == ')') {
        return new SptToken(PM_RPAR, PM_VALUE_RPAR, st.lineno());
      } else if (c == ':') {
        return new SptToken(PM_COLON, PM_VALUE_COLON, st.lineno());
      } else if (c == '-') {
        d = st.nextToken();
        if (d == '>') {
          return new SptToken(PM_ARROW, PM_VALUE_ARROW, st.lineno());
        } else {
          st.pushBack();
          return new SptToken(PM_UNKNOWN, c, st.lineno());
        }
      } else if (c == ';') {
        return new SptToken(PM_SEMICOLON, PM_VALUE_SEMICOLON, st.lineno());
      } else if (c == '<') {
        d = st.nextToken();
        if (d == '=') {
          return new SptToken(PM_LESS_THAN_OR_EQUAL, PM_VALUE_LESS_THAN_OR_EQUAL, st.lineno());
        } else {
          st.pushBack();
          return new SptToken(PM_LESS_THAN, PM_VALUE_LESS_THAN, st.lineno());
        }
      } else if (c == '>') {
        d = st.nextToken();
        if (d == '=') {
          return new SptToken(PM_LESS_THAN_OR_EQUAL, PM_VALUE_LESS_THAN_OR_EQUAL, st.lineno());
        } else {
          st.pushBack();return new SptToken(PM_GREATER_THAN, PM_VALUE_GREATER_THAN, st.lineno());
        }
      }
      else {
        return new SptToken(PM_UNKNOWN, c, st.lineno());
      }
    } catch (Exception e) {
      e.printStackTrace();
      return new SptToken(PM_ERROR, "error", st.lineno());
    }
  }
  
  public static String getTokenValue(int id) {
    if (id >= PM_FIRST_KEYWORD && id <= PM_LAST_KEYWORD) {
      return sKeywords[id - PM_FIRST_KEYWORD];
    } else
    switch (id) {
      case PM_ARROW:
        return PM_VALUE_ARROW;
      case PM_COMMA:
        return PM_VALUE_COMMA;
      case PM_EQUAL:
        return PM_VALUE_EQUAL;
      case PM_LPAR:
        return PM_VALUE_LPAR;
      case PM_RPAR:
        return PM_VALUE_RPAR;
      case PM_WORD:
        return PM_VALUE_WORD;
      case PM_SEMICOLON:
        return PM_VALUE_SEMICOLON;
      case PM_LESS_THAN:
        return PM_VALUE_LESS_THAN;
      case PM_GREATER_THAN:
        return PM_VALUE_GREATER_THAN;
      case PM_LESS_THAN_OR_EQUAL:
        return PM_VALUE_LESS_THAN_OR_EQUAL;
      case PM_GREATER_THAN_OR_EQUAL:
        return PM_VALUE_GREATER_THAN_OR_EQUAL;
      default:
        return PM_VALUE_UNKNOWN;
    }
  }

  public int lineno() {
    return st.lineno();
  }
}
