package org.parser;

import java.util.*;
import org.generic.*;

/**
 * Generates the parser table based on inputed grammar rules.
 * 
 * @author Elizabeth White, Andrew Guyton, Andrew Leonard
 * @date 2010/26/4
 */
public class TokenParserTable {
	// class variables
	// array of rules
	public HashMap<String,ArrayList<Rule>>[][] table;
	public ArrayList<Token> tokens;
	public ArrayList<Nonterminal> nonterminals;
	public Nonterminal startSymbol;
	
	public void setTokens(ArrayList<Token> tok)
	{
		tokens = tok;
	}
	
	public void setNonterminals(ArrayList<Nonterminal> nont)
	{
		nonterminals = nont;
	}
	
	// add rule R to M[A, a] in the table
    public void addRule(Nonterminal A, Token a, Rule R)
    {
    	
    }
	
	public Rule getRule(Nonterminal nt, Token t)
	{
		try
		{
			// try to find a rule that corresponds to these symbols
			return table[tokens.indexOf(t)][nonterminals.indexOf(nt)].get(nt + "," + t).get(0);
		}
		catch(NullPointerException e)
		{
			// can't find a rule like that
			return null;
		}
	}
	
	public void setStartSymbol(Nonterminal start)
	{
		startSymbol = start; 
	}
	
	public Nonterminal getStartSymbol() 
	{
		return startSymbol;
	}
}
