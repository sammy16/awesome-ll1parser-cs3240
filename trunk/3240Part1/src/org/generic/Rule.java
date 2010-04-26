package org.generic;

import java.util.ArrayList;

public class Rule {
	private Nonterminal left;
	private ArrayList<Symbol> right;
	
	// creates a rule
	public Rule(Nonterminal l, ArrayList<Symbol> r)
	{
		left = l;
		right = r;
	}
	
	// prints out the rule
	public String toString()
	{ 
		String value = left.toString() + " : ";
		for(int i=0; i<right.size(); i++)
		{
			if( i>0 && i < (right.size() - 1) )
			{
				value += " ";
			}
			
			value += right.get(i);
		}
		
		return left.toString() + ":" + right.toString(); 
	}
	
	public Nonterminal getLeft()
	{
		return left;
	}
	
	public ArrayList<Symbol> getRight()
	{
		return right;
	}
}
