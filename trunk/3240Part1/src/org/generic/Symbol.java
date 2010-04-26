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
	
	// identically-named symbols are assumed to be identical
	public boolean equals(Symbol s)
	{
		return s.name.equals(name);
	}
}
