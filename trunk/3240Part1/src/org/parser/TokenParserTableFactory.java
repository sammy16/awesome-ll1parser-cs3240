package org.parser;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;

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
		// read in the tokens and nonterminals and store them appropriately
		String tokens = readLine(buffer);
		ArrayList<Token> tok = new ArrayList<Token>();
		String[] temp = tokens.split(" ");
		for(int i=0; i<temp.length; i++)
		{
			Token t = new Token();
			t.TokenParse(temp[i]);
			tok.add(t);
		}
		table.setTokens(tok);
		
		String nonterminals = readLine(buffer);
		ArrayList<Nonterminal> nont = new ArrayList<Nonterminal>();
		String[] temp2 = nonterminals.split(" ");
		for(int i=0; i<temp2.length; i++)
		{
			Nonterminal n = new Nonterminal(temp2[i]);
			nont.add(n);
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
		ArrayList<String> rules;
		rules = removeLeftRecursion(grammarRules);
		rules = removeCommonPrefix(rules);
		
		// add the rules to the parse table now?
	}
	
	// magical function that removes left recursion
	private ArrayList<String> removeLeftRecursion(ArrayList<String> rules)
	{
		return null;
	}
	
	// magical function that removes common prefixes
	private ArrayList<String> removeCommonPrefix(ArrayList<String> rules)
	{
		return null;
	}
	
	public TokenParserTable getParsingTable()
	{
		return table;
	}
}
