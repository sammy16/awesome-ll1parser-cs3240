package org.parser2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.generic.Nonterminal;
import org.generic.Rule;
import org.generic.Symbol;
import org.generic.Token;
import org.parser2.ParsingTable;

public class ParsingTableBuilder {
	private ParsingTable table;
	
	private ArrayList<Nonterminal> gNonterminals;
    private Symbol gStartSymbol;
    private HashMap<Nonterminal, ArrayList<Rule>> gRules;
	
    private HashMap<Rule, ArrayList<Token>> predictSets;
	
	@SuppressWarnings("unchecked")
	public ParsingTableBuilder(ArrayList<Token> tokens, ArrayList<Nonterminal> nonterminals, Symbol startSymbol, HashMap<Nonterminal, ArrayList<Rule>> rules) {
		gNonterminals = nonterminals;
		//gTokens = tokens;
		gStartSymbol = startSymbol;
		gRules = rules;
		
		 // compute first() sets
		HashMap<Nonterminal, ArrayList<Token>> firstSets = computeFirstSets();
	    
	    // compute follow() sets
		HashMap<Nonterminal, ArrayList<Token>> followSets = computeFollowSets(firstSets);
	    
	    // compute predict() sets using first() and follow() sets
	    predictSets = computePredictSets(followSets,firstSets);
	    
	    // initialize new ParsingTable
	    table = new ParsingTable(tokens.size(),nonterminals.size(),tokens,nonterminals, startSymbol);
	    
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
	}
	
	public ParsingTable getTable() {
		return table;
	}
	
	public Symbol getStartSymbol() {
		return gStartSymbol;
	}
	

    private HashMap<Nonterminal, ArrayList<Token>> computeFirstSets() {
        HashMap<Nonterminal, ArrayList<Token>> firstSetsLocal = new HashMap<Nonterminal, ArrayList<Token>>();
        for (Nonterminal N : gNonterminals) {
            ArrayList<Token> temp = first(N);
            firstSetsLocal.put(N, temp);
        }
        return firstSetsLocal;
    }
    
	// Compute Follow sets for all nonterminals
    private HashMap<Nonterminal, ArrayList<Token>> computeFollowSets(HashMap<Nonterminal, ArrayList<Token>> firstSets)
    {
        HashMap<Nonterminal, ArrayList<Token>> followSetsLocal = new HashMap<Nonterminal, ArrayList<Token>>();
        
        // initialize follow sets here
        // follow(startSymbol) = {$}, and the rest are empty
        for (Nonterminal N : gNonterminals) {
            if (N.getName().equals(gStartSymbol.getName())) {
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
            for (Nonterminal N : gNonterminals) 
            {
            	if(gRules.get(N) != null)
            	{
	                for (Rule prodn : gRules.get(N)) 
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
	                            
	                            if (allNullable(prodend, firstSets)) 
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
	                            ArrayList<Token> first = first(followSymbols, firstSets);
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
    
    // page 178 Louden
    private HashMap<Rule, ArrayList<Token>> computePredictSets(
    		HashMap<Nonterminal, ArrayList<Token>> followSets , 
    		HashMap<Nonterminal, ArrayList<Token>> firstSets) 
    {
        HashMap<Rule, ArrayList<Token>> predictSetsLocal = new HashMap<Rule, ArrayList<Token>>();
        
        ArrayList<Nonterminal> keyList = new ArrayList<Nonterminal>();
        ArrayList<Rule> rules = new ArrayList<Rule>();
        keyList.addAll(gRules.keySet());

        for (Nonterminal key : keyList) {
            rules.addAll(gRules.get(key));
            
            if (!gNonterminals.contains(key))
            	gNonterminals.add(key);
        }
        
        // we compute the predict set for each production rule in the grammar
        for (Rule P : rules) {
            ArrayList<Token> temp = new ArrayList<Token>();
            
            Nonterminal A = P.getLeft();
            ArrayList<Symbol> alpha = P.getRight();
            // to start, we get the first set of the right-hand side
            ArrayList<Token> first = first(alpha, firstSets);
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
    
    
    
    // a symbol string is nullable if the whole thing can through some
    // series of productions be mapped to epsilon
    private boolean allNullable(ArrayList<Symbol> symbols, HashMap<Nonterminal, ArrayList<Token>> firstSets) 
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
        if(gRules.get(S) != null)
        {
	        for (Rule R : gRules.get((Nonterminal)S)) {
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
    private ArrayList<Token> first(ArrayList<Symbol> alpha, HashMap<Nonterminal, ArrayList<Token>> firstSets) 
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

}
