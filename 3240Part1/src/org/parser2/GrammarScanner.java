package org.parser2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import org.generic.Nonterminal;
import org.generic.Symbol;
import org.generic.Token;

public class GrammarScanner {
	private ArrayList<Token> allTokens = new ArrayList<Token>();
	private ArrayList<Nonterminal> allNonT = new ArrayList<Nonterminal>();
	private Symbol startSymbol;
	private ArrayList<String> rawRules = new ArrayList<String>();
	
	public GrammarScanner(String fileLocation) {
		URL trueLocation = this.getClass().getResource(fileLocation);
		InputStreamReader ins;
		try {
			ins = new InputStreamReader(trueLocation.openStream());
			BufferedReader grFileBuf = new BufferedReader(ins);
			readTokens(grFileBuf);
			readNonT(grFileBuf);
			readStartSymbol(grFileBuf);
			readRules(grFileBuf);
		} catch (IOException e) {
			System.out.println("Grammar file not found!");
		}
	}
	
	private void readTokens(BufferedReader buffer) {
		try {
			String toks = buffer.readLine();
			String[] toksList = toks.split(" ");
			for ( int i = 1; i < toksList.length ; i++) {
				allTokens.add(new Token(toksList[i], i));
			}
		} catch (IOException e) {
			System.out.println("Grammer File Buffer Error (Tokens)!");
		}
	}
	private void readNonT(BufferedReader buffer) {
		try {
			String nonTerms = buffer.readLine();
			String[] nonTermsList = nonTerms.split(" ");
			for ( int i = 1; i < nonTermsList.length ; i++) {
				allNonT.add(new Nonterminal(nonTermsList[i]));
			}
		} catch (IOException e) {
			System.out.println("Grammer File Buffer Error (Non-terminals)!");
		}
	}
	private void readStartSymbol(BufferedReader buffer) {
		try {
			String startS = buffer.readLine();
			String[] startSlist = startS.split(" ");
			startSymbol = new Symbol(startSlist[1]);
		} catch (IOException e) {
			System.out.println("Grammer File Buffer Error (StartSymbol)!");
		}
	}
	private void readRules(BufferedReader buffer) {
		try {
			String rule;
			while( (rule = buffer.readLine()) != null)
			{
				if(!rule.startsWith("%Rules"))
				{
					rawRules.add(rule);
				}
			}
			//System.out.println("Grammar Raw Rules = " + rawRules);
		} catch (IOException e) {
			System.out.println("Grammer File Buffer Error (Rules)!");
		}		
	}
	
	public Symbol getStartSymbol() {
		return startSymbol;
	}
	public ArrayList<Nonterminal> getNonTerminals() {
		return allNonT;
	}
	public ArrayList<Token> getTokens() {
		return allTokens;
	}
	public ArrayList<String> getRawRules() {
		return rawRules;
	}
}
