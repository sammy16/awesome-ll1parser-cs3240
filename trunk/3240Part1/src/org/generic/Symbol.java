package org.generic;

/**
 * Superclass for Token and Nonterminal
 * 
 * @author Andrew Guyton
 * @date 2010/18/4
 */ 
public class Symbol {
	// name of the symbol
	protected String name;
	/**
	 * This method constructs this class
	 * @param String s name of Symbol
	 */
	public Symbol(String s) {
		name = s;
	}
	
	/**
	 * Returns true if both symbols are identically-named
	 * @param Symbol symbol were comparing this to
	 */
	public boolean equals(Symbol s)
	{
		if(s.name == null && this.name == null)
		{
			return true;
		}
		else if(s.name == null || this.name == null)
		{
			return false;
		}
		else
		{
			return s.name.equals(this.name);
		}
	}
	
	/**
	 * Returns the symbol's name
	 * @return name of the Symbol
	 */
	public String getName() {
		return name;
	}
	/**
	 * Returns the symbol's name by overriding the toString() function.
	 * @return name of the Symbol
	 */
	public String toString()
	{
		return name;
	}
}
