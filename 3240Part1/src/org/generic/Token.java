package org.generic;

/**
 * The tokens used in the parser.
 * 
 * @author Elizabeth White
 * @date 2010/18/4
 */
public class Token extends Symbol {
	private Kind kind;
	@SuppressWarnings("unused")
	private int nval;
	
	//private String name;
	
	/**
	 * This method constructs this class
	 */
	public Token(Kind k) { 
		super(null);
		kind = k; 
		name = kind.name();
		nval = 0;
	}
	
	/**
	 * This method constructs this class if the Token is an INTNUM
	 */
	public Token(int n) { 
		super(null);
		kind = Kind.INTNUM;
		nval = n;
	}
	
	/**
	 * This method constructs this class if the Token is an ID
	 */
	public Token(String s) { 
		super(s);
		kind = Kind.ID;
		nval = 0;
	}
	
	/**
	 * This method constructs this class if the Token is an unknown/ERROR
	 */
	public Token() {
		super(null);
		kind = Kind.ERROR;
		nval = 0;
	}
	
	/**
	 * This method attempts to construct a token from a string
	 * @param String s string the token is contructed from
	 * @param int throwaway param (IT HAS NO POINT!)
	 */
	public Token(String s, int n)
	{
		super("");
		TokenParse(s);
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
			return kind.toString();
		}
		else if ( kind == Kind.DOLLAR)
		{
			return "$";
		}
		else if ( kind == Kind.INTNUM ) {
			return kind.toString();
		}
		else {
			return kind.toString();
		}
	}
	/**
	 * This method returns whether this Token matches the given Token
	 * @param Token s Token to compare this Token to
	 * @return boolean
	 */
	public boolean equals(Token s)
	{
		//System.out.println(s.kind + ", " + this.kind);
		
		if(s.kind == null && this.kind == null)
		{
			return true;
		}
		else if(s.kind == null || this.kind == null)
		{
			return false;
		}
		else
		{
			return s.kind.equals(this.kind);
		}
	}
	/**
	 * This method returns what kind of Token this is
	 * @return Kind
	 */
	public Kind getKind() {
		return this.kind;
	}
}
