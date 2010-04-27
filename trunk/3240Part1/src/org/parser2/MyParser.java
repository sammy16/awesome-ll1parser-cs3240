package org.parser2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import org.generic.Kind;
import org.generic.Nonterminal;
import org.generic.Rule;
import org.generic.Symbol;
import org.generic.Token;

public class MyParser {
	private ArrayList<Token> input;
	private ArrayList<Nonterminal> gNonterminals;
    private ArrayList<Token> gTokens;
    private Symbol gStartSymbol;
    private HashMap<Nonterminal, ArrayList<Rule>> gRules;
    
    private ParsingTableBuilder ptb;
	
	public MyParser(ArrayList<Token> tokens, String grammarFileLocation) {
		input = tokens;
		
		// Read the grammar file
		GrammarScanner gs = new GrammarScanner(grammarFileLocation);
		gNonterminals = gs.getNonTerminals();
		gTokens = gs.getTokens();
		gStartSymbol = gs.getStartSymbol();
		//printGrammar();
		
		//System.out.println("Grammar Raw Rules = " + gs.getRawRules());
		
		// Validate the grammar rules
		GrammarConverter gc = new GrammarConverter(gs.getRawRules(), gNonterminals, gTokens);
		gRules = gc.convertRawRules();
		//printGrammarRules(gRules);
		
		// Build the parsing table
		ptb = new ParsingTableBuilder(gTokens, gNonterminals, gStartSymbol, gRules);
		//printTable(ptb.getTable());
		
		//Computer input based on parsing table
	}
	
	/**
	 * Based on Louden LL(1) parsing algorithm on page 155 
	 */
	public void algorithm() 
	{
		Stack<Symbol> stack = new Stack<Symbol>();
		int count = 0;
		stack.push(new Token("$"));
		//Nonterminal nta = table.getStartSymbol();
		Symbol nta = ptb.getStartSymbol();
		stack.push(new Nonterminal(nta.getName()));
		while( !stack.peek().equals(new Token("$")) && count < input.size() ) {
			System.out.println("Current token: " + input.get(count));
			System.out.println("Current stack: " + stack);
			// [Case 1]: Top of stack is a token
			if( stack.peek() instanceof Token) {
				if(stack.peek().equals(new Token(Kind.EPSILON))) {
					stack.pop();
				}
				else 
				{
					Token tokinput = input.get(count);
					Token tokpeek = (Token)stack.peek(); 
					if(tokpeek.equals(tokinput)) 
					{
						System.out.println("Popping off the stack: " + tokpeek);
						stack.pop();
						count++;
					}
					else {
						break;
					}
				}
			}
			// [Case 2]: Top of stack is non-terminal
			else if ( stack.peek() instanceof Nonterminal) 
			{
				Nonterminal nextNonTerm = (Nonterminal)stack.peek();
				Token nextToken = input.get(count);
				System.out.println(nextNonTerm + ", " + nextToken);
				
				Rule r = ptb.getTable().getRule( nextNonTerm, nextToken );
				System.out.println("Rule: " + r);
				if(r == null) 
				{
					break;
				}
				stack.pop();
				ArrayList<Symbol> symbols = r.getRight();
				for (int k = (symbols.size() - 1); k >= 0; k--) 
				{
					stack.push(symbols.get(k));
				}
			}
		}
		if (stack.peek().equals(new Token("$")) && count == input.size() - 1) {
			System.out.println("TokenParser: Successful parse");
		} else {
			System.out.println("TokenParser: Parsing error");
			System.out.println("Current token (Error): " + input.get(count));
			System.out.println("Current stack (Error): " + stack);
		}
	}
	
	
	public void printGrammar() {
		System.out.println("Grammar Token List = " + gTokens);
		System.out.println("Grammar List of Nonterminals = " + gNonterminals);
		System.out.println("Grammar Start Symbol = " + gStartSymbol);
	}
	
	public void printGrammarRules(HashMap<Nonterminal, ArrayList<Rule>> grammar) {
        for (Nonterminal N : grammar.keySet()) {
            for (Rule R : grammar.get(N)) {
                System.out.println(R);
            }
        }
    }
}
