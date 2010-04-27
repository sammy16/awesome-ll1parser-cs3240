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
		gStartSymbol = startSymbol;
		gRules = rules;
		
		HashMap<Nonterminal, ArrayList<Token>> firstSets = computeFirstSets();
	    
		HashMap<Nonterminal, ArrayList<Token>> followSets = computeFollowSets(firstSets);
	    
	    predictSets = computePredictSets(followSets,firstSets);
	    
	    table = new ParsingTable(tokens.size(),nonterminals.size(),tokens,nonterminals, startSymbol);
	    
	    Collection ruls = predictSets.keySet();
	    Iterator r = ruls.iterator();
	    
	    Rule nonT;
	    ArrayList<Token> tks;
	    while(r.hasNext())
	    {
	        nonT = (Rule)r.next();
	        tks = predictSets.get(nonT);
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
    
    private HashMap<Nonterminal, ArrayList<Token>> computeFollowSets(HashMap<Nonterminal, ArrayList<Token>> firstSets)
    {
        HashMap<Nonterminal, ArrayList<Token>> followSetsLocal = new HashMap<Nonterminal, ArrayList<Token>>();
        
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
        
      
        boolean changed = true;
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
	                            ArrayList<Symbol> followSymbols = new ArrayList<Symbol>(prodelements.subList(i+1, k));
	                            ArrayList<Token> first = first(followSymbols, firstSets);
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
	                        }
	                    }
	                }
            	}
            }
        }
        return followSetsLocal;
    }
    
    //from louden 178
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
        
        for (Rule P : rules) {
            ArrayList<Token> temp = new ArrayList<Token>();
            
            Nonterminal A = P.getLeft();
            ArrayList<Symbol> alpha = P.getRight();
            ArrayList<Token> first = first(alpha, firstSets);
            for (Token T : first)
                temp.add(T);
            
            if (first.contains(new Token("EPSILON"))) {
                for (Token T : followSets.get(A))
                    temp.add(T);
            }
            predictSetsLocal.put(P, temp);
        }
        
        return predictSetsLocal;
    }
    
    
    
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
        if (S instanceof Token) {
            ArrayList<Token> singleton = new ArrayList<Token>();
            singleton.add((Token)S);
            return singleton;
        }
        
        HashSet<Token> ret = new HashSet<Token>();
        if(gRules.get(S) != null)
        {
	        for (Rule R : gRules.get((Nonterminal)S)) {
	            ArrayList<Symbol> symbols = R.getRight();
	            boolean hasEpsilon = false;
	            for (Symbol X_i : symbols) {
	                hasEpsilon = false;
	                for (Token T : first(X_i)) {
	                    if (T.getName().equals("EPSILON"))
	                        hasEpsilon = true;
	                    else
	                        ret.add(T);
	                }
	                if (!hasEpsilon)
	                    break;
	            }
	            
	            if (hasEpsilon)
	                ret.add(new Token("EPSILON"));
	        }
        }
        
	    ArrayList<Token> retAsList = new ArrayList<Token>();
	    retAsList.addAll(ret);
	    return retAsList;

    }
    
    //from louden 168
    private ArrayList<Token> first(ArrayList<Symbol> alpha, HashMap<Nonterminal, ArrayList<Token>> firstSets) 
    {
        if (alpha.size() == 0) {   
            ArrayList<Token> temp = new ArrayList<Token>();
            return temp;
        }
       
        if (alpha.size() == 1 && alpha.contains(new Token("EPSILON"))) {
            ArrayList<Token> temp = new ArrayList<Token>();
            temp.add(new Token("EPSILON"));
            return temp;
        }
        
       HashSet<Token> ret = new HashSet<Token>();
       ArrayList<Token> firstAlpha = first(alpha.get(0));
       ret.addAll(firstAlpha);
       
       boolean hasEpsilon = ret.contains(new Token("EPSILON"));
       ret.remove(new Token("EPSILON"));
       int i = 1;
       while (hasEpsilon && i < alpha.size()) {
           ArrayList<Token> nextFirstSet = null;
           Symbol nextSymbol = alpha.get(i);
           if (nextSymbol instanceof Nonterminal) {
               nextFirstSet = firstSets.get((Nonterminal)nextSymbol);
           }
           else
               nextFirstSet = first(nextSymbol);
           
           if (!nextFirstSet.contains(new Token("EPSILON")))
               hasEpsilon = false;
           for (Token T : nextFirstSet)
               ret.add(T);
           ret.remove(new Token("EPSILON"));
           i++;
       }
       
       ArrayList<Token> retAsList = new ArrayList<Token>();
       retAsList.addAll(ret);
       return retAsList;
    }

}
