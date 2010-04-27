package org.parser2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
		//printHTMLTable(ptb.getTable());
		
		//Computer input based on parsing table
	}
	public void algorithm() 
	{
		Stack<Symbol> stack = new Stack<Symbol>();
		int count = 0;
		stack.push(new Token("$"));
		//Nonterminal nta = table.getStartSymbol();
		Symbol nta = ptb.getStartSymbol();
		stack.push(new Nonterminal(nta.getName()));
		while( !stack.peek().equals(new Token("$")) && count < input.size() ) {
			//System.out.println("Current token: " + input.get(count));
			//System.out.println("Current stack: " + stack);
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
				Nonterminal nontnext = (Nonterminal)stack.peek();
				Token mytoken = input.get(count);
				System.out.println(nontnext + ", " + mytoken);
				
				Rule r = ptb.getTable().getRule( (Nonterminal)stack.peek(), input.get(count) );
				System.out.println("Rule: " + r);
				if(r == null) 
				{
					break;
				}
				stack.pop();
				ArrayList<Symbol> symbols = r.getRight();
				for (int k = (symbols.size() - 1); k >= 0; k--) {
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
	
	public void printHTMLTable(ParsingTable table)
    {
        System.out.println();
        System.out.println("Writing parse table to file...");
        try {
            BufferedWriter html = new BufferedWriter(new FileWriter(new File("ptable.html")));
            html.write("<table border=1>");
            html.write("<tr><td>M[N,T]</td>");

            for (Token T : gTokens) {
                html.write("<td>" + T + "</td>");
            }
            html.write("</tr>");

            for(int x=0;x<gNonterminals.size();x++) {
                html.write("<tr>");
                //first entry in the row is the row's nonterminal
                html.write("<td>" + fixHTML(gNonterminals.get(x).toString()) + "</td>");


                for(int y=0;y<gTokens.size();y++) {
                    //print row of rules in coresponding location
                    String rowRule = (table.getGrid())[x][y].get((table.getGrid())[x][y].keySet().toArray()[0]).toString();
                    html.write("<td>" + fixHTML(rowRule) + "</td>");

                }
                html.write("</tr>");
            }
            html.write("</table>");
            html.close();
            System.out.println("Success! The parse table has been written to ptable.html in the project directory.");
        //System.out.println(html);
        } catch (IOException e) {
            System.out.println("File writing failed. Please make sure you have write access to the project directory.");
        }
        System.out.println();
    }
    
    // nonterminals like "<exp>" look like HTML tags, so we have to replace
    // > and < with metacharacters &gt; and &lt;
    private String fixHTML(String tag) {
        return tag.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    }

}
