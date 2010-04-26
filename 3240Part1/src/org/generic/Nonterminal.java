package org.generic;

/**
 * The nonterminals used in the parser.
 * 
 * @author Andrew Guyton
 * @date 2010/18/4
 */
public class Nonterminal extends Symbol {
	
	public Nonterminal(String c)
	{
		name = c;
	}
	
	public String toString()
	{
		return name;
	}
}
