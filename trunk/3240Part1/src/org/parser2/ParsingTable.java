package org.parser2;

import java.util.ArrayList;
import java.util.HashMap;

import org.generic.Nonterminal;
import org.generic.Rule;
import org.generic.Symbol;
import org.generic.Token;

public class ParsingTable {
	private HashMap<String,ArrayList<Rule>>[][] table;
	
	private ArrayList<Nonterminal> gNonterminals;
    private ArrayList<Token> gTokens;
	
	private int rows,columns;
	
	@SuppressWarnings("unchecked")
	public ParsingTable(int tokensSize, int nontermSize, ArrayList<Token> tokens,
			ArrayList<Nonterminal> nonterminals, Symbol startSymbol) {
		tokensSize++;
		tokens.add(new Token("$"));
		
		rows = tokensSize;
		columns = nontermSize;
		
		gNonterminals = nonterminals;
		gTokens = tokens;
		
		table = (HashMap<String,ArrayList<Rule>>[][]) new HashMap[columns][rows];
		//System.out.println("Col: " + columns + " Rows: " + rows);
		for(int i = 0; i < rows; i++)
		{
			for(int j = 0; j < columns; j++)
			{
				table[j][i] = new HashMap<String,ArrayList<Rule>>();
				String nonTname = nonterminals.get(j).getName();
				//Token tokra = tokens.get(i);
				String tokName = tokens.get(i).getName();
				String key = nonTname + "," + tokName;
				table[j][i].put(key, new ArrayList<Rule>());
			}
		}
	}

	public HashMap<String,ArrayList<Rule>>[][] getGrid() {
		return table;
	}
	
	public void addRule(Nonterminal left, Token t, Rule nonT) {
		String ruleEntry = left.getName() + "," + t.getName();
		for(int i = 0; i < columns; i++)
		{
			for(int j = 0; j < rows; j++)
			{
				if(table[i][j].containsKey(ruleEntry))
				{
					table[i][j].get(ruleEntry).add(nonT);
				}
			}
		}
		return;
	}
	
	public Rule getRule(Nonterminal peek, Token token) 
	{
		System.out.println("Finding rule for: " + peek + ", " + token);
		try 
		{
			int x = -1;
			int y = -1;
			for(int i=0; i<gTokens.size(); i++)
			{
				Token tok = gTokens.get(i);
				if(tok.equals(token))
				{
					x = i;
					break;
				}
			}
			for(int i=0; i<gNonterminals.size(); i++)
			{
				if(gNonterminals.get(i).equals(peek))
				{
					y = i;
					break;
				}
				
			}
			if( x == -1 || y == -1) 
			{
				return null;
			}
			ArrayList<Rule> rule = table[x][y].get(peek + "," + token);
			return rule.get(0);
		} catch (NullPointerException npe) {
			System.out.println("Can't find rule for " + peek + " -> " + token);
			return null;
		} catch (IndexOutOfBoundsException iobe) {
			System.out.println(iobe.getMessage());
			return null;
		}
	}
}
