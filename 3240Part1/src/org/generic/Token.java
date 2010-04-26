package org.generic;

/**
 * The tokens used in the parser.
 * 
 * @author Elizabeth White
 * @date 2010/18/4
 */
public class Token extends Symbol {
	private Kind kind;
	private int nval;
	
	//private String name;
	
	/**
	 * This method constructs this class
	 */
	public Token(Kind k) { 
		kind = k; 
		nval = 0;
		name = null;
	}
	
	/**
	 * This method constructs this class if the Token is an INTNUM
	 */
	public Token(int n) { 
		kind = Kind.INTNUM;
		nval = n;
		name = null;
	}
	
	/**
	 * This method constructs this class if the Token is an ID
	 */
	public Token(String s) { 
		kind = Kind.ID;
		nval = 0;
		name = s;
	}
	
	/**
	 * This method constructs this class if the Token is an unknown/ERROR
	 */
	public Token() {
		kind = Kind.ERROR;
		nval = 0;
		name = null;
	}
	
	/**
	 * This method sets the kind according to a string that is passed in
	 */
	public void TokenParse(String s)
	{
		if (s.equalsIgnoreCase("begin"))
		{
			kind = Kind.BEGIN;
			name = "BEGIN";
		}
		else if(s.equalsIgnoreCase("end"))
		{
			kind = Kind.END;
			name = "END";
		}
		else if(s.equalsIgnoreCase("assign"))
		{
			kind = Kind.ASSIGN;
			name = "ASSIGN";
		}
		else if(s.equalsIgnoreCase("intnum"))
		{
			kind = Kind.INTNUM;
			name = "INTNUM";
		}
		else if(s.equalsIgnoreCase("comma"))
		{
			kind = Kind.COMMA;
			name = "COMMA";
		}
		else if(s.equalsIgnoreCase("semicolon"))
		{
			kind = Kind.SEMICOLON;
			name = "SEMICOLON";
		}
		else if(s.equalsIgnoreCase("leftpar"))
		{
			kind = Kind.LEFTPAR;
			name = "LEFTPAR";
		}
		else if(s.equalsIgnoreCase("rightpar"))
		{
			kind = Kind.RIGHTPAR;
			name = "RIGHTPAR";
		}
		else if(s.equalsIgnoreCase("plus"))
		{
			kind = Kind.PLUS;
			name = "PLUS";
		}
		else if(s.equalsIgnoreCase("minus"))
		{
			kind = Kind.MINUS;
			name = "MINUS";
		}
		else if(s.equalsIgnoreCase("multiply"))
		{
			kind = Kind.MULTIPLY;
			name = "MULTIPLY";
		}
		else if(s.equalsIgnoreCase("modulo"))
		{
			kind = Kind.MODULO;
			name = "MODULO";
		}
		else if(s.equalsIgnoreCase("ID"))
		{
			kind = Kind.ID;
			name = "ID";
		}
		else if(s.equalsIgnoreCase("read"))
		{
			kind = Kind.READ;
			name = "READ";
		}
		else if(s.equalsIgnoreCase("print"))
		{
			kind = Kind.PRINT;
			name = "PRINT";
		}
		else if(s.equalsIgnoreCase("dollar"))
		{
			kind = Kind.DOLLAR;
			name = "$";
		}
	}
	
	/**
	 * This method returns the String of the Token based on it's Kind
	 * @return String string of the Token
	 */
	public String toString() {
		if ( kind == Kind.ID ) {
			return name;
		}
		else if ( kind == Kind.INTNUM ) {
			return Integer.toString(nval);
		}
		else {
			return kind.toString();
		}
	}
}
