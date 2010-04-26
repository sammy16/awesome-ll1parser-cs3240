package org.parser;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JOptionPane;

import org.generic.*;

/**
 * Helps create the parser table based on inputed grammar rules.
 * 
 * @author Elizabeth White, Andrew Guyton, Andrew Leonard
 * @date 2010/26/4
 */
public class TokenParserTableFactory {
	public TokenParserTable table;
	
	public TokenParserTableFactory()
	{
		table = new TokenParserTable();
	}
	
	// reads a file that specifies the grammar of a language
	public void Load(String filename) 
	{
		BufferedReader buffer;
		try
		{
			buffer = new BufferedReader(new FileReader(filename));
			parseFile(buffer);
		} catch(FileNotFoundException e)
		{
			JOptionPane.showMessageDialog(null, "File not found: " + filename);
		}
	}

	// reads a file that specifies the grammar of a language
	public void LoadURL(URL url) 
	{
		BufferedReader buffer;
		InputStreamReader ins;
		try
		{
			ins = new InputStreamReader(url.openStream());
			buffer = new BufferedReader(ins);
			parseFile(buffer);
		} catch(FileNotFoundException e)
		{
			JOptionPane.showMessageDialog(null, "File not found: " + url);
		} catch (IOException e) 
		{
			JOptionPane.showMessageDialog(null, "IO Error: " + url);
		}
	}
	
	// reads in lines until it finds one such non-blank line
	// allows for some flexibility in the file format
	private String readLine(BufferedReader buffer)
	{
		String value = "";
		
		// try to read a non-blank line
		try
		{
	        do
	        {
	        	value = buffer.readLine();
	        }
	        while (value.trim().equals(""));
		}
		catch(IOException e)
		{
			// can't read
			return null;
		}
		catch(NullPointerException e)
		{
			// trimming a null
			return null;
		}
        
		// return the line
        return value;
	}
	
	// specifies how to parse the file
	private void parseFile(BufferedReader buffer)
	{
		// read in the tokens and store them
		String tokens = readLine(buffer);
		ArrayList<Token> tok = new ArrayList<Token>();
		String[] temp = tokens.split(" ");
		// i=0 is %TOKENS, skip it
		for(int i=1; i<temp.length; i++)
		{
			if(!temp[i].trim().equals(""))
			{
				Token t = new Token();
				t.TokenParse(temp[i]);
				tok.add(t);
			}
		}
		table.setTokens(tok);
		
		// read in nonterminals and store them
		String nonterminals = readLine(buffer);
		ArrayList<Nonterminal> nont = new ArrayList<Nonterminal>();
		String[] temp2 = nonterminals.split(" ");
		// i=0 is %NONTERMINALS, skip it
		for(int i=1; i<temp2.length; i++)
		{
			if(!temp2[i].trim().equals(""))
			{
				Nonterminal n = new Nonterminal(temp2[i]);
				nont.add(n);
			}
		}
		table.setNonterminals(nont);
		
		// read in the start symbol
		String startSymbol = readLine(buffer);
		startSymbol = startSymbol.substring(6);
		table.setStartSymbol(new Nonterminal(startSymbol));
		System.out.println("Start symbol:" + startSymbol);
		
		// read in the grammar rules
		String rule;
		ArrayList<String> grammarRules = new ArrayList<String>();
		while( (rule = readLine(buffer)) != null)
		{
			if(!rule.startsWith("%"))
			{
				// skip to the next line if we're on a comment
				grammarRules.add(rule);
			}
		}
		
		// fix them by removing left recursion and common prefix
		HashMap<Nonterminal, ArrayList<Rule>> rules;
		rules = removeLeftRecursion(grammarRules);
		rules = removeCommonPrefix(rules);
		
		// add the rules to the parse table now?
	}
	
	// converts a string to a rule that we understand
	public ArrayList<Rule> getRules(String rawRule)
	{
		ArrayList<Rule> result = new ArrayList<Rule>();
		
		int colon = rawRule.indexOf(":");
		String left = rawRule.substring(0, colon).trim();
		Nonterminal n = new Nonterminal(left);
		
		String right = rawRule.substring(colon+1).trim();
		String rightarr[] = right.split("\\|", -1);
		for(int i=0; i<rightarr.length; i++)
		{
			String thisrule = rightarr[i].trim();
			
			
		}
		
		return result;
	}
	
	/*
	 * Removes instances of left recursion from the grammar; 
	 * described on Louden p 158 
	 */
	private HashMap<Nonterminal, ArrayList<Rule>> removeLeftRecursion(ArrayList<String> rawRules)
	{	
		ArrayList<Rule> rules = new ArrayList<Rule>();
		for(String lineRule : rawRules)
		{
			ArrayList<Rule> rule = this.getRules(lineRule);
			rules.addAll(rule);
		}
		
		return null;
	}
	
	// magical function that removes common prefixes
	private HashMap<Nonterminal, ArrayList<Rule>> removeCommonPrefix(HashMap<Nonterminal, ArrayList<Rule>> rules)
	{
		return null;
	}
	
	public TokenParserTable getParsingTable()
	{
		return table;
	}
}
