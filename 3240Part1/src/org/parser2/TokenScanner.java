package org.parser2;
import java.util.ArrayList;

import org.generic.Kind;
import org.generic.Token;

/**
 * Converts a String into a list of tokens
 * 
 * @author Elizabeth White, Andrew Guyton, Andrew Leonard
 * @date 2010/26/4
 */
public class TokenScanner {
	private ArrayList<Token> tokens = new ArrayList<Token>();
	private ArrayList<String> words = new ArrayList<String>();
	private String input;
	/**
	 * This method constructs this class
	 * @param String input String
	 */
	public TokenScanner(String s) {
		this.input = s;
		input = input.replaceAll(";", " ; ");
		input = input.replaceAll("\\,", " , ");
		input = input.replaceAll("\\(", " ( ");
		input = input.replaceAll("\\)", " ) ");
		input = input.replaceAll(":=", " := ");
		input = input.replaceAll("\\+", " + ");
		input = input.replaceAll("\\-", " - ");
		input = input.replaceAll("\\*", " * ");
		input = input.replaceAll("\\%", " % ");
		input = input.replaceAll("  ", " ");
	}
	/**
	 * This method returns the list of tokens
	 * @return ArrayList<Token> list of tokens
	 */
	public ArrayList<Token> getTokens() {
		String[] temp = input.split(" ");
		for(int i = 0; i < temp.length ; i++) {
			words.add(temp[i]);
		}
		convert2tokens();
		return tokens;
	}
	/**
	 * This method converts the list of words to list of tokens
	 */
	private void convert2tokens() {
		for(int j = 0; j < words.size(); j++) {
			Token token = new Token();
			if (words.get(j).compareToIgnoreCase("begin") == 0) {
				token = new Token(Kind.BEGIN);
			} else if (words.get(j).compareToIgnoreCase("end") == 0) {
				token = new Token(Kind.END);
			} else if (words.get(j).compareToIgnoreCase("read") == 0) {
				token = new Token(Kind.READ);
			} else if (words.get(j).compareToIgnoreCase("print") == 0) {
				token = new Token(Kind.PRINT);
			} else if (words.get(j).compareToIgnoreCase(":=") == 0) {
				token = new Token(Kind.ASSIGN);
			} else if (words.get(j).compareToIgnoreCase(";") == 0) {
				token = new Token(Kind.SEMICOLON);
			} else if (words.get(j).compareToIgnoreCase(",") == 0) {
				token = new Token(Kind.COMMA);
			} else if (words.get(j).compareToIgnoreCase("%") == 0) {
				token = new Token(Kind.MODULO);
			} else if (words.get(j).compareToIgnoreCase("*") == 0) {
				token = new Token(Kind.MULTIPLY);
			} else if (words.get(j).compareToIgnoreCase("+") == 0) {
				token = new Token(Kind.PLUS);
			} else if (words.get(j).compareToIgnoreCase("-") == 0) {
				token = new Token(Kind.MINUS);
			} else if (words.get(j).compareToIgnoreCase("(") == 0) {
				token = new Token(Kind.LEFTPAR);
			} else if (words.get(j).compareToIgnoreCase(")") == 0) {
				token = new Token(Kind.RIGHTPAR);
			} else {
				try 
				{
					int number = Integer.parseInt(words.get(j));
					token = new Token(number);
				} catch (NumberFormatException nfe) {
					if( validateID(words.get(j)) ) {
						token = new Token(words.get(j));
					} else {
						token = new Token();
					}
				}
			}
			tokens.add(token);
		}
	}
	/**
	 * This method validates a string as a proper id
	 * @return boolean string is valid or not
	 */
	private boolean validateID(String id) {
		int size = id.length();
		char firstCh = id.charAt(0);
		char lastCh = id.charAt(size-1);
		// id's can not be longer than 10 chars
		if ( size <= 10 ) {
			// id's must start with letter or underscore
			if ( firstCh == '_' || Character.isLetter(firstCh) ) {
				// id's can't end with underscore
				if ( lastCh != '_' ) {
					// id's may have digits, letters, or underscores
					// underscore must be followed by a letter or digit
					for ( int i = 0 ; i < size ; i++ ) {
						char currCh = id.charAt(i);
						if( Character.isDigit(currCh) || Character.isLetter(currCh) || 
								( currCh == '_' && id.charAt(i+1)!= '_' ) ) {
								return true;
						}						
					}
				}
			}
		}
		return false;
	}
}
