package org.generic;

/**
 * The tokens used in the parser.
 * 
 * @author Elizabeth White
 * @date 2010/18/4
 */
public class Token {
	private Kind kind;
	private int nval;
	private String sval;
	/**
	 * This method constructs this class
	 */
	public Token(Kind k) { 
		kind = k; 
		nval = 0;
		sval = null;
	}
	/**
	 * This method constructs this class if the Token is an INTNUM
	 */
	public Token(int n) { 
		kind = Kind.INTNUM;
		nval = n;
		sval = null;
	}
	/**
	 * This method constructs this class if the Token is an ID
	 */
	public Token(String s) { 
		kind = Kind.ID;
		nval = 0;
		sval = s;
	}
	/**
	 * This method constructs this class if the Token is an unknown/ERROR
	 */
	public Token() {
		kind = Kind.ERROR;
		nval = 0;
		sval = null;
	}
	/**
	 * This method returns the String of the Token based on it's Kind
	 * @return String string of the Token
	 */
	public String toString() {
		if ( kind == Kind.BEGIN ) {
			return "BEGIN";
		}
		else if ( kind == Kind.END ) {
			return "END";
		}
		else if ( kind == Kind.ASSIGN ) {
			return "ASSIGN";
		}
		else if ( kind == Kind.INTNUM ) {
			return Integer.toString(nval);
		}
		else if ( kind == Kind.COMMA ) {
			return ",";
		}
		else if ( kind == Kind.SEMICOLON ) {
			return ";";
		}
		else if ( kind == Kind.LEFTPAR ) {
			return "(";
		}
		else if ( kind == Kind.RIGHTPAR ) {
			return ")";
		}
		else if ( kind == Kind.PLUS ) {
			return "+";
		}
		else if ( kind == Kind.MINUS ) {
			return "-";
		}
		else if ( kind == Kind.MULTIPLY ) {
			return "*";
		}
		else if ( kind == Kind.MODULO ) {
			return "%";
		}
		else if ( kind == Kind.ID ) {
			return sval;
		}
		else if ( kind == Kind.READ ) {
			return "READ";
		}
		else if ( kind == Kind.PRINT ) {
			return "PRINT";
		}
		else if ( kind == Kind.ERROR ) {
			return "ERROR";
		}
		else if ( kind == Kind.DOLLAR ) {
			return "$";
		}
		else {
			return kind.toString();
		}
	}
}
