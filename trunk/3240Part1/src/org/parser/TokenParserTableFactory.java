package org.parser;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

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
	Nonterminal startSymbol;
    private ArrayList<Nonterminal> nonterminals;
    private ArrayList<Token> tokens;
    private HashMap<Nonterminal, ArrayList<Rule>> allRules;
	private HashMap<Nonterminal, ArrayList<Token>> firstSets;
    private HashMap<Nonterminal, ArrayList<Token>> followSets;
    private HashMap<Rule, ArrayList<Token>> predictSets;
    private ArrayList<Rule> classRules;
	
	public TokenParserTableFactory()
	{
		classRules = new ArrayList<Rule>();
		//table = new TokenParserTable();
	}
	
	/*
	 * Return the parsing table
	 */
	public TokenParserTable getParsingTable()
	{
		return table;
	}
	
	public Nonterminal getStartSymbol()
	{
		return startSymbol;
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
		String tokensStr = readLine(buffer);
		tokens = new ArrayList<Token>();
		String[] temp = tokensStr.split(" ");
		// i=0 is %TOKENS, skip it
		for(int i=1; i<temp.length; i++)
		{
			if(!temp[i].trim().equals(""))
			{
				Token t = new Token();
				t.TokenParse(temp[i]);
				tokens.add(t);
			}
		}
		//table.setTokens(tok);
		
		// read in nonterminals and store them
		String nonterminalsStr = readLine(buffer);
		nonterminals = new ArrayList<Nonterminal>();
		String[] temp2 = nonterminalsStr.split(" ");
		// i=0 is %NONTERMINALS, skip it
		for(int i=1; i<temp2.length; i++)
		{
			String substring = temp2[i].trim();
			
			if(!substring.equals(""))
			{
				Nonterminal n = new Nonterminal(substring);
				nonterminals.add(n);
			}
		}
		//table.setNonterminals(nont);
		
		// read in the start symbol
		String startSymbolStr = readLine(buffer);
		startSymbolStr = startSymbolStr.split(" ")[1].trim();
		startSymbol = new Nonterminal(startSymbolStr);
		System.out.println("Start symbol: " + startSymbol);
		
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
		allRules = removeLeftRecursion(grammarRules);
		allRules = removeCommonPrefix(allRules);
		
		// set up the key list
        ArrayList<Nonterminal> keyList = new ArrayList<Nonterminal>();
        keyList.addAll(allRules.keySet());

        for (Nonterminal key : keyList) 
        {
            classRules.addAll(allRules.get(key));
            
            if (!nonterminals.contains(key))
            {
                nonterminals.add(key);
            }
        }
	}
	
	/*
	 * converts a string to a rule (or rules, in the case of A -> B | C )
	 * that we understand and can use to manipulate symbols
	 */
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
			String looprule = rightarr[i].trim();
			ArrayList<Symbol> loopsymbols = new ArrayList<Symbol>();
			if(looprule.equals(""))
			{
				// empty rule --> add epsilon
				loopsymbols.add(new Token(Kind.EPSILON));
			}
			else
			{
				// non-empty rule: add ArrayList of symbols
				String symbols[] = looprule.split(" ");
				for(int j=0; j<symbols.length; j++)
				{
					String subrule = symbols[j].trim();
					if(tokens.contains(new Token(subrule, 0)))
					{
						loopsymbols.add(new Token(subrule, 0));
					}
					else if(tokens.contains(new Nonterminal(subrule)))
					{
						loopsymbols.add(new Nonterminal(subrule));
					}
				}
			}
			
			result.add(new Rule(n, loopsymbols));
		}
		
		return result;
	}
	
	/*
	 * Removes instances of left recursion from the grammar; 
	 * described on Louden p 158 
	 */
	@SuppressWarnings("unchecked")
	private HashMap<Nonterminal, ArrayList<Rule>> removeLeftRecursion(ArrayList<String> rawRules)
	{	
		// iterate through each line and add any rules you find
		ArrayList<Rule> rules = new ArrayList<Rule>();
		for(String lineRule : rawRules)
		{
			ArrayList<Rule> rule = this.getRules(lineRule);
			rules.addAll(rule);
		}
		
		HashMap<Nonterminal, ArrayList<Rule>> rulesMap = new HashMap<Nonterminal, ArrayList<Rule>>();
		Nonterminal nontermrule;
		ArrayList<Rule> newRulesList = new ArrayList<Rule>();
		int i = 0;
		
		while(i<rules.size())
		{
			nontermrule = rules.get(i).getLeft();
			while(rules.get(i).getLeft().getName().equals(nontermrule.getName())){
				newRulesList.add(rules.get(i));
				i++;
				
				if(i >= rules.size()){
					break;
				}
			}
			rulesMap.put(nontermrule, newRulesList);
		}
		
		ArrayList<Nonterminal> keyList = new ArrayList<Nonterminal>();
		keyList.addAll(rulesMap.keySet());
		for(Nonterminal key : keyList){
			
			ArrayList<Rule> sameRulesList = rulesMap.get(key);
			boolean hasLeftRecursion = this.NonterminalHasLeftRecursion(key, sameRulesList);
			
			if (hasLeftRecursion) {
                Nonterminal aPrime = new Nonterminal(key.getName() + "'");
                
                Rule aPrimeEpsilonRule = new Rule(aPrime, new ArrayList<Symbol>());
                aPrimeEpsilonRule.getRight().add(new Token("EPSILON"));
                
                ArrayList<Rule> aPrimeRules = new ArrayList<Rule>();
                aPrimeRules.add(aPrimeEpsilonRule);
                
                rulesMap.put(aPrime, aPrimeRules);
                
                for (Rule rule : ((ArrayList<Rule>) sameRulesList.clone())){                   
                	Symbol startingSymbol = rule.getRight().get(0);
	                if(startingSymbol.equals(key)) {
                        sameRulesList.remove(rule);
                        rule.getRight().remove(0);
                        ArrayList<Symbol> newRule = new ArrayList<Symbol>();
                        newRule.addAll(rule.getRight());
                        newRule.add(aPrime);
                        Rule newPrimeRule = new Rule(aPrime, newRule);
                        aPrimeRules.add(newPrimeRule);
                     } else {
                        sameRulesList.remove(rule);
                        ArrayList<Symbol> newRuleList = new ArrayList<Symbol>();
                        newRuleList.addAll(rule.getRight());
                        newRuleList.add(aPrime);
                        Rule newARule = new Rule(key, newRuleList);
                        sameRulesList.add(newARule);
                     }
                }
			}
		}
		
		return rulesMap;
	}
	
	/*
	 * Checks to see if a given nonterminal has left recursion
	 */
    private boolean NonterminalHasLeftRecursion(Nonterminal nonterm, ArrayList<Rule> sameRules) {
        boolean hasLeftRecursion = false;
        for(Rule rule : sameRules) 
        {
        	ArrayList<Symbol> rightrules = rule.getRight();
        	if(rightrules.size()>0)
        	{
        		Symbol left = rightrules.get(0);
        		if(left.equals(nonterm)) 
        		{         
        			hasLeftRecursion = true;                        
        		}
        	}
        }
        return hasLeftRecursion;       
    }
	
	/* 
	 * Removes common refixes from a set of rules
	 */
	private HashMap<Nonterminal, ArrayList<Rule>> removeCommonPrefix(HashMap<Nonterminal, ArrayList<Rule>> rules)
	{
		boolean madeChange = false;
		ArrayList<Nonterminal> keyList = new ArrayList<Nonterminal>();
		keyList.addAll(rules.keySet());
		for (Nonterminal key : keyList )
		{
			ArrayList<Rule> matchingRules = rules.get(key);
			ArrayList<Symbol> cp = new ArrayList<Symbol>();
			
			//ArrayList<Rule> commonPrefRules = getMaximalCommonPrefixProductionRules(matchingRules --> rules, cp);
			ArrayList<Rule> commonPrefRules;
	    	if(matchingRules.size() == 1 || matchingRules.size() == 0)
	    	{
	    		// if there can't be matches, use empty list
	    		commonPrefRules = new ArrayList<Rule>(); 
	    	}       
	    
	    	ArrayList<ArrayList<Symbol>> foundPrefix = new ArrayList<ArrayList<Symbol>>();
	    	for(Rule outer : matchingRules)
	    	{
	            for(Rule inner : matchingRules)
	            {              
	                ArrayList<Symbol> commonPrefix = new ArrayList<Symbol>();
	                commonPrefix(commonPrefix, outer.getRight(), inner.getRight());
	                if(!containsPrefix(foundPrefix, commonPrefix))
	                {
	                	if(commonPrefix.size() > 0)
	                	{                            
	                        foundPrefix.add(commonPrefix);
	                    }
	                }
	            }
	    	}
			
	    	ArrayList<Symbol> maxPrefix = prefixOfMaximalLength(foundPrefix);
	    	if(maxPrefix != null)
	    	{
	            cp.addAll(maxPrefix);
	    	}
	    	commonPrefRules = getRulesStartingWith(maxPrefix, matchingRules);
	    	// end getMaximalCommonPrefixProductionRules
	    	
            if(commonPrefRules.size() > 1)
            { 
            	// if we've found some production rules that have commonPrefix
            	madeChange = true;
            	//replace with A => cp Beta1 | cp Beta2 |... We use the hashcode for the star
            	// with A => cp A*
            	//              A* =>Beta1 | Beta2 | EPSILON
            	Nonterminal aStar = new Nonterminal(key.getName() + key.getName().hashCode());
	                        
	            //create the new rule that replaces all the production rules associated with the common prefix
	            ArrayList<Symbol> replacementRule = new ArrayList<Symbol>();
	            replacementRule.addAll(cp);
	            replacementRule.add(aStar);
	            Rule replacementPR = new Rule(key, replacementRule);
	            matchingRules.removeAll(commonPrefRules);
	            matchingRules.add(replacementPR);
	                        
	            ArrayList<Rule> aStarPRS = new ArrayList<Rule>();
	            for(Rule r : commonPrefRules)
	            {
	                ArrayList<Symbol> newStarRule = new ArrayList<Symbol>();
	                for(int i = cp.size(); i < r.getRight().size(); i++)
	                {
	                    newStarRule.add(r.getRight().get(i));
	                }
	                if(newStarRule.size() == 0)
	                {
	                	newStarRule.add(new Token("EPSILON", 0));
	                }
	                Rule newStarPR = new Rule(aStar, newStarRule);
                    aStarPRS.add(newStarPR);
                }
                rules.put(aStar, aStarPRS);
        	}
    	}
	        
        if(madeChange)
        {
            removeCommonPrefix(rules);
        }
        else
        {
            return rules;
        }
        
        return rules; // should never get here
	}
    
    private ArrayList<Rule> getRulesStartingWith(ArrayList<Symbol> prefix, ArrayList<Rule> rules)
    {
        ArrayList<Rule> retColl = new ArrayList<Rule>();
        if(prefix != null)
        {
        	for(Rule pr : rules)
        	{
                boolean mismatch = false;
                if(pr.getRight().size() >= prefix.size())
                {
                    for(int i=0; i < prefix.size() - 1; i++)
                    {
                        if(!prefix.get(i).equals(pr.getRight().get(i)))
                        {
                            mismatch = true;
                            break;
                        }
                    }
                    if(!mismatch)
                    {
                        retColl.add(pr);
                    }
                }
            }
        }
        return retColl;
    }
	
    private ArrayList<Symbol> prefixOfMaximalLength(ArrayList<ArrayList<Symbol>> prefixes)
    {
        int maxSize = 0;
        ArrayList<Symbol> maxP = null;
        for(ArrayList<Symbol> p : prefixes)
        {
            if(maxSize < p.size())
            {
                maxSize = p.size();
                maxP = p;
            }
        }
        
        return maxP;
    }
    
    private boolean containsPrefix(ArrayList<ArrayList<Symbol>> prefixes, ArrayList<Symbol> p)
    {
        for(ArrayList<Symbol> containedPre : prefixes)
        {
            if(p.equals(containedPre))
            {
                return true;
            }
        }
        
        return false;
    }
    
    private void commonPrefix(ArrayList<Symbol> commonPrefix, ArrayList<Symbol> rule1, ArrayList<Symbol> rule2)
    {    
        if(rule1.size() == 0 || rule2.size() == 0 || rule1.equals(rule2) )
        {
            return;
        }
        
        if( rule1.get(0).equals(rule2.get(0)))
        {
            ArrayList<Symbol> r1Clone = new ArrayList<Symbol>(rule1);
            ArrayList<Symbol> r2Clone = new ArrayList<Symbol>(rule2);
            r1Clone.remove(0);
            r2Clone.remove(0);
            commonPrefix.add(rule1.get(0));
            commonPrefix(commonPrefix, r1Clone, r2Clone);
        }
    }
    
    @SuppressWarnings("unchecked")
	public TokenParserTable buildParsingTable() 
    {
        // compute first() sets
        firstSets = computeFirstSets();
        
        // compute follow() sets
        followSets = computeFollowSets();
        
        // compute predict() sets using first() and follow() sets
        predictSets = computePredictSets();
        
        // initialize new ParsingTable
        table = new TokenParserTable(tokens.size(),nonterminals.size(),tokens,nonterminals);
        table.setStartSymbol(startSymbol);
        
        //iterator for the rules of the predict set
        Collection ruls = predictSets.keySet();
        Iterator r = ruls.iterator();
        
        // iterate through [rule, predict_set] key-value pairs in hashtable
        Rule nonT;
        ArrayList<Token> tks;
        while(r.hasNext())
        {
            nonT = (Rule)r.next();
            tks = predictSets.get(nonT);   //list of tokens for each rule
            // iterate through tokens of predict set and add entry in parse table for the production rule
            for(Token t: tks)
            {
                table.addRule(nonT.getLeft(), t, nonT);
            }
        }
        return table;
    }

    private HashMap<Nonterminal, ArrayList<Token>> computeFirstSets() {
        HashMap<Nonterminal, ArrayList<Token>> firstSetsLocal = new HashMap<Nonterminal, ArrayList<Token>>();
        for (Nonterminal N : nonterminals) {
            ArrayList<Token> temp = first(N);
            firstSetsLocal.put(N, temp);
        }
        return firstSetsLocal;
    }
    
    private ArrayList<Token> first(Symbol S) {
        // S is any terminal (which includes epsilon)
        if (S instanceof Token) {
            ArrayList<Token> singleton = new ArrayList<Token>();
            singleton.add((Token)S);
            return singleton;
        }
        
        HashSet<Token> ret = new HashSet<Token>();
        // okay, so it's a nonterminal
        // iterate through each rule for this nonterminal
        if(allRules.get(S) != null)
        {
	        for (Rule R : allRules.get((Nonterminal)S)) {
	            // okay, we are on a particular rule
	            ArrayList<Symbol> symbols = R.getRight();
	            boolean hasEpsilon = false;
	            // now, for each symbol in that rule sequence...
	            for (Symbol X_i : symbols) {
	                hasEpsilon = false;
	                // we add the First set the current symbol
	                // if that First set contains epsilon, we note that, so that
	                // we can get the First set of the NEXT symbol, too.
	                for (Token T : first(X_i)) {
	                    if (T.getName().equals("EPSILON"))
	                        hasEpsilon = true;
	                    else
	                        ret.add(T);
	                }
	                if (!hasEpsilon)
	                    break;
	            }
	            // if hasEpsilon is true, this implies that every symbol in the
	            // sequence had epsilon in its First set, and so the First set
	            // for the whole rule must contain epsilon, as well.
	            if (hasEpsilon)
	                ret.add(new Token("EPSILON"));
	        }
        }
        
	    ArrayList<Token> retAsList = new ArrayList<Token>();
	    retAsList.addAll(ret);
	    return retAsList;

    }
    
    /**
     * Louden page 168
     * This method assumes computeFirstSets() has already been called,
     * creating a cache of first(N) for each nonterminal.
     * @param alpha
     * @return
     */
    private ArrayList<Token> first(ArrayList<Symbol> alpha) 
    {
        if (alpha.size() == 0) {   
            ArrayList<Token> temp = new ArrayList<Token>();
            //temp.add(new Token("EPSILON"));
            return temp;
        }
       
        // special case: alpha is a set containing only epsilon
        if (alpha.size() == 1 && alpha.contains(new Token("EPSILON"))) {
            ArrayList<Token> temp = new ArrayList<Token>();
            temp.add(new Token("EPSILON"));
            return temp;
        }
        
       // we use a set so that duplicates are automatically ignored
       HashSet<Token> ret = new HashSet<Token>();
       ArrayList<Token> firstAlpha = first(alpha.get(0));
       ret.addAll(firstAlpha);
       
       boolean hasEpsilon = ret.contains(new Token("EPSILON"));
       ret.remove(new Token("EPSILON"));
       int i = 1;
       while (hasEpsilon && i < alpha.size()) {
           ArrayList<Token> nextFirstSet = null;
           Symbol nextSymbol = alpha.get(i);
           // if it's a nonterminal, it's already computed and cached
           if (nextSymbol instanceof Nonterminal) {
               nextFirstSet = firstSets.get((Nonterminal)nextSymbol);
           }
           // if it's a terminal, it will be computed instantly anyway
           else
               nextFirstSet = first(nextSymbol);
           
           if (!nextFirstSet.contains(new Token("EPSILON")))
               hasEpsilon = false;
           for (Token T : nextFirstSet)
               ret.add(T);
           ret.remove(new Token("EPSILON"));
           i++;
       }
       
       //return removeDups(ret);
       ArrayList<Token> retAsList = new ArrayList<Token>();
       retAsList.addAll(ret);
       return retAsList;
    }

    // Compute Follow sets for all nonterminals
    private HashMap<Nonterminal, ArrayList<Token>> computeFollowSets()
    {
        HashMap<Nonterminal, ArrayList<Token>> followSetsLocal = new HashMap<Nonterminal, ArrayList<Token>>();
        
        // initialize follow sets here
        // follow(startSymbol) = {$}, and the rest are empty
        for (Nonterminal N : nonterminals) {
            if (N.getName().equals(startSymbol.getName())) {
                ArrayList<Token> temp = new ArrayList<Token>();
                temp.add(new Token("$"));
                followSetsLocal.put(N, temp);
            }
            else {
                followSetsLocal.put(N, new ArrayList<Token>());
            }
        }
        
        //ArrayList<Token> ret = new ArrayList<Token>();
        
        // we keep track of whether any changes are made to the follow sets during each pass
        // when no changes have been made, we're at stasis and we terminate
        boolean changed = true;
        // while the follow sets are still "active" (changing), keep making passes
        while (changed) {
            changed = false;
            for (Nonterminal N : nonterminals) 
            {
            	if(allRules.get(N) != null)
            	{
	                for (Rule prodn : allRules.get(N)) 
	                {
	                    ArrayList<Symbol> prodelements = prodn.getRight();
	                    int k = prodelements.size();
	                    for (int i = 0; i < k; i++) {
	                        Symbol S = prodelements.get(i);
	                        if (S instanceof Nonterminal) 
	                        {
	                            Nonterminal X = (Nonterminal)S;
	                            ArrayList<Token> xFollow = followSetsLocal.get(X);
	                            ArrayList<Symbol> prodend = new ArrayList<Symbol>(prodelements.subList(i+1, k));
	                            
	                            if (allNullable(prodend)) 
	                            {
	                                ArrayList<Token> nFollow = followSetsLocal.get(N);
	                                // add N follow set to X follow set; check for changes
	                                boolean changed2 = false;
	                                for (Token T : nFollow) 
	                                {
	                                    if (!xFollow.contains(T)) 
	                                    {
	                                        changed2 = true;
	                                        xFollow.add(T);
	                                    }
	                                }
	                                changed = changed || changed2;
	                            }
	                            // first set
	                            ArrayList<Symbol> followSymbols = new ArrayList<Symbol>(prodelements.subList(i+1, k));
	                            ArrayList<Token> first = first(followSymbols);
	                            // add first set to xFollow; check for changes
	                            boolean changed3 = false;
	                            for (Token T : first) 
	                            {
	                                if (!xFollow.contains(T)) 
	                                {
	                                    changed3 = true;
	                                    xFollow.add(T);
	                                }
	                            }
	                            changed = changed || changed3;
	                            followSetsLocal.put(X, xFollow);
	                        }//end if
	                    }
	                }
            	}
            }
        }
        return followSetsLocal;
    }
    
    // a symbol string is nullable if the whole thing can through some
    // series of productions be mapped to epsilon
    private boolean allNullable(ArrayList<Symbol> symbols) 
    {
        for (Symbol S : symbols) 
        {
            if (S instanceof Token && !S.getName().equals("EPSILON"))
                return false;
            if (S instanceof Nonterminal) 
            {
                if (!firstSets.get((Nonterminal)S).contains(new Token("EPSILON")))
                    return false;
            }
        }
        return true;
    }
    
    // page 178 Louden
    private HashMap<Rule, ArrayList<Token>> computePredictSets() 
    {
        HashMap<Rule, ArrayList<Token>> predictSetsLocal = new HashMap<Rule, ArrayList<Token>>();
        // we compute the predict set for each production rule in the grammar
        for (Rule P : classRules) {
            ArrayList<Token> temp = new ArrayList<Token>();
            
            Nonterminal A = P.getLeft();
            ArrayList<Symbol> alpha = P.getRight();
            // to start, we get the first set of the right-hand side
            ArrayList<Token> first = first(alpha);
            for (Token T : first)
                temp.add(T);
            
            // if that first set contains alpha, then we also add the follow
            // set to the predict set.
            if (first.contains(new Token("EPSILON"))) {
                for (Token T : followSets.get(A))
                    temp.add(T);
            }
            // hash it up!
            predictSetsLocal.put(P, temp);
        }
        
        return predictSetsLocal;
    }
    
    public void printGrammar() {
        for (Nonterminal N : allRules.keySet()) {
            for (Rule R : allRules.get(N)) {
                System.out.println(R);
            }
        }
    }
}