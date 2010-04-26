package org.generic;

/**
 * The nonterminals used in the parser.
 * 
 * @author Andrew Guyton
 * @date 2010/18/4
 */
public class Nonterminal {
	private String content;
	
	public Nonterminal(String c)
	{
		content = c;
	}
	
	public String toString()
	{
		return content;
	}
}
