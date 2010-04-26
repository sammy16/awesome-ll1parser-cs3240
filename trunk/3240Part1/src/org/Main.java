package org;

import java.util.ArrayList;

import org.generic.Token;
import org.parser.TokenParser;
import org.parser.TokenScanner;
/**
 * The main class for the LL(1) parser generator for CS 3240 Project.
 * 
 * @author Elizabeth White, Andrew Guyton, Andrew Leonard
 * @date 2010/26/4
 */
public class Main {
	/**
	 * Runs the application.
	 * @param args
	 */
	public static void main(String[] args) {
		String input = "begin a := a + b; end";
		System.out.println("Input: " + input);
		TokenScanner ts = new TokenScanner(input);
		ArrayList<Token> answer = ts.getTokens();
		System.out.println("Input Tokens: " + answer.toString());
		TokenParser parser = new TokenParser(answer);
		parser.algorithm();
	}
}
