package org.parser;

import java.net.URL;
import java.util.ArrayList;
import java.util.Stack;

import org.generic.*;
/**
 * Parses a list of tokens based on the TokenParserTable.
 * 
 * @author Elizabeth White, Andrew Guyton, Andrew Leonard
 * @date 2010/26/4
 */
public class TokenParser {
	private ArrayList<Token> input = new ArrayList<Token>();
	private Stack<Symbol> stack = new Stack<Symbol>();
	//private TokenParserTableFactory factory;
	private ParserGenerator factory;
	private TokenParserTable table;
	
	public TokenParser(ArrayList<Token> t) {
		// tokens from constructor
		input = t;
		
		// parse the language definition into a parsing table
		//TokenParserTableFactory factory = new TokenParserTableFactory();
		factory = new ParserGenerator();
		String location = "/org/resources/tiny.txt";
		URL trueLocation = this.getClass().getResource(location);
		//String fullLocation = trueLocation.toExternalForm();
		//System.out.println(fullLocation);
		//factory.LoadURL(trueLocation);
		try {
			factory.feed(trueLocation);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// get the parsing table from the factory
		table = factory.buildParsingTable();
		table.printHTMLTable();
		//table.toString();
	}
	
	public void algorithm() 
	{
		int count = 0;
		stack.push(new Token("$"));
		//Nonterminal nta = table.getStartSymbol();
		Symbol nta = factory.getStartSymbol();
		stack.push(new Nonterminal(nta.getName()));
		while( !stack.peek().equals(new Token("$")) && count < input.size() ) {
			//System.out.println("Current token: " + input.get(count));
			//System.out.println("Current stack: " + stack);
			// [Case 1]: Top of stack is a token
			if( stack.peek() instanceof Token) {
				if(stack.peek().equals(new Token(Kind.EPSILON))) {
					stack.pop();
				}
				else {
					if(stack.peek().equals(input.get(count))) {
						stack.pop();
						count++;
					}
					else {
						break;
					}
				}
			}
			// [Case 2]: Top of stack is non-terminal
			else if ( stack.peek() instanceof Nonterminal) {
				Rule r = table.getRule( (Nonterminal)stack.peek(), input.get(count) );
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
}
