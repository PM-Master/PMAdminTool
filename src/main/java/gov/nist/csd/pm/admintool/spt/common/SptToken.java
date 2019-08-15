package gov.nist.csd.pm.admintool.spt.common;

/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:02:58 $
 * @since 1.5
 */
public class SptToken {
  /**
 * @uml.property  name="tokenClass"
 */
public int tokenClass;
  /**
 * @uml.property  name="tokenSubclass"
 */
public int tokenSubclass;
  /**
 * @uml.property  name="tokenId"
 */
public static int tokenId;
  /**
 * @uml.property  name="tokenValue"
 */
public String tokenValue;
  /**
 * @uml.property  name="tokenLineno"
 */
public int tokenLineno;
  
  public SptToken(int id, int lineno) {
    tokenId = id;
    tokenLineno = lineno;
  }

  public SptToken(int id, String s, int lineno) {
    tokenId = id;
    tokenValue = s;
    tokenLineno = lineno;
  }

  public SptToken(int id, int c, int lineno) {
    char[] ch = {(char)c};
    tokenId = id;
    tokenValue = new String(ch);
    tokenLineno = lineno;
  }
}