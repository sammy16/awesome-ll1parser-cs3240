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
		return table[tokens.indexOf(t)][nonterminals.indexOf(nt)].get(nt + "," + t).get(0);
	}
	
	public Symbol getStartSymbol() {
		return null;
	}
}
