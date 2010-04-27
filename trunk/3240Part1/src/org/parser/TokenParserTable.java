package org.parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.generic.*;

/**
 * Generates the parser table based on inputed grammar rules.
 * 
 * @author Elizabeth White, Andrew Guyton, Andrew Leonard
 * @date 2010/26/4
 */
public class TokenParserTable {
	
	// class variables
	// array of rules
	private HashMap<String,ArrayList<Rule>>[][] table;
	private ArrayList<Token> tokens;
	private ArrayList<Nonterminal> nonterminals;
	private Nonterminal startSymbol;
	
	private int rows,columns;
	
	@SuppressWarnings("unchecked")
	public TokenParserTable(int tokSize, int nonTsize, ArrayList<Token> toks,
			ArrayList<Nonterminal> nonTs) {
		tokSize++;
		toks.add(new Token("$"));
		
		rows = nonTsize;
		columns = tokSize;
		
		tokens = toks;
		nonterminals = nonTs;
		
		table = (HashMap<String,ArrayList<Rule>>[][]) new HashMap[columns][rows];
        
//		for (int y=0;y<rows;y++) {
//			if(largestNonterminal < nonterminals.get(y).getName().length())
//			{
//				largestNonterminal = nonterminals.get(y).getName().length();
//			}
//		}
		for(int i = 0; i < columns; i++)
		{
			for(int j = 0; j < rows; j++)
			{
				table[i][j] = new HashMap<String,ArrayList<Rule>>();
				String key = nonTs.get(j).getName() + "," + toks.get(i).getName();
				table[i][j].put(key, new ArrayList<Rule>());
			}
		}
	}
	public void addRule(Nonterminal left, Token t, Rule nonT) {
		String ruleEntry = left.getName() + "," + t.getName();
		//int numRules;
		for(int i = 0; i < columns; i++)
		{
			//numRules = 0;
			for(int j = 0; j < rows; j++)
			{
				if(table[i][j].containsKey(ruleEntry))
				{
					table[i][j].get(ruleEntry).add(nonT);
//					numRules++;
//					if(nonT.getRight().toString().length()*numRules > largestEntrySize)
//					{
//						largestEntrySize = nonT.toString().length() * numRules;
//					}
				}
			}
		}
		return;
	}
	public Rule getRule(Nonterminal peek, Token token) {
		try {
			int x = tokens.indexOf(token);
			int y = nonterminals.indexOf(peek);
			if( x == -1 || y == -1) {
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
	public Nonterminal getStartSymbol() {
		return startSymbol;
	}
	public void setStartSymbol(Nonterminal start) {
		startSymbol = start;
	}
	
	public void printHTMLTable()
    {
        System.out.println();
        System.out.println("Writing parse table to file...");
        try {
            BufferedWriter html = new BufferedWriter(new FileWriter(new File("ptable.html")));
            html.write("<table border=1>");
            html.write("<tr><td>M[N,T]</td>");

            for (Token T : tokens) {
                html.write("<td>" + T + "</td>");
            }
            html.write("</tr>");

            for(int x=0;x<rows;x++) {
                html.write("<tr>");
                //first entry in the row is the row's nonterminal
                html.write("<td>" + fixHTML(nonterminals.get(x).toString()) + "</td>");


                for(int y=0;y<columns;y++) {
                    //print row of rules in coresponding location
                    String rowRule = table[y][x].get(table[y][x].keySet().toArray()[0]).toString();
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
