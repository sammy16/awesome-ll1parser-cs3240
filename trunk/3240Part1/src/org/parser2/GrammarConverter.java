package org.parser2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import org.generic.Nonterminal;
import org.generic.Rule;
import org.generic.Symbol;
import org.generic.Token;

public class GrammarConverter {
	private ArrayList<String> rawRules = new ArrayList<String>();
	private HashMap<Nonterminal, ArrayList<Rule>> grammarRules;
	private ArrayList<Nonterminal> grammarNonterminals;
    private ArrayList<Token> grammarTokens;
    
	public GrammarConverter(ArrayList<String> rules, ArrayList<Nonterminal> nonterminals, ArrayList<Token> tokens) {
		rawRules = rules;
		grammarNonterminals = nonterminals;
		grammarTokens = tokens;
	}
	
	public HashMap<Nonterminal, ArrayList<Rule>> convertRawRules() {
		grammarRules = removeLeftRecursion(rawRules);
		grammarRules = removeCommonPrefix(grammarRules);
		return grammarRules;
	}
	
	private HashMap<Nonterminal, ArrayList<Rule>> removeCommonPrefix(
            HashMap<Nonterminal, ArrayList<Rule>> map) {

		boolean madeChange = false;
		ArrayList<Nonterminal> keyList = new ArrayList<Nonterminal>();
		keyList.addAll(map.keySet());
		for (Nonterminal key : keyList )
		{
		    ArrayList<Rule> matchingRules = map.get(key);
		    ArrayList<Symbol> cp = new ArrayList<Symbol>();
		    ArrayList<Rule> commonPrefRules = getMaximalCommonPrefixRules(matchingRules, cp);
		    if(commonPrefRules.size() > 1){ // if we've found some production rules that have commonPrefix
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
		            for(Rule r : commonPrefRules){
		                    ArrayList<Symbol> newStarRule = new ArrayList<Symbol>();
		                    for(int i = cp.size(); i < r.getRight().size(); i++){
		                            newStarRule.add(r.getRight().get(i));
		                    }
		                    if(newStarRule.size() == 0){
		                            //newStarRule.add(Symbol.EPSILON);
		                            newStarRule.add(new Token("EPSILON"));
		                    }
		                    Rule newStarPR = new Rule(aStar, newStarRule);
		                    aStarPRS.add(newStarPR);
		            }
		            map.put(aStar, aStarPRS);
		    }
		}
		
	    if(madeChange){
	            removeCommonPrefix(map);
	    }
	    else{
	            return map;
	    }
	    return map; // should never get here
	}

	@SuppressWarnings("unchecked")
	private HashMap<Nonterminal, ArrayList<Rule>> removeLeftRecursion(ArrayList<String> gRules) {
        ArrayList<Rule> prRules = new ArrayList<Rule>();
        for(String lineRule : gRules )
        {
        		//System.out.println(this.getRules(lineRule));
                ArrayList<Rule> prodRules = this.getRules(lineRule);
                prRules.addAll(prodRules);
        }
        
        HashMap<Nonterminal, ArrayList<Rule>> map = this.hashRulesByNonterminal(prRules);
        ArrayList<Nonterminal> keyList = new ArrayList<Nonterminal>();
        keyList.addAll(map.keySet());
        for (Nonterminal key : keyList )
        {       
                ArrayList<Rule> matchingRules = map.get(key);
                //ArrayList<Rule> newMatchingRules = new ArrayList<Rule>();
                //ArrayList<Rule> primeRules = new ArrayList<Rule>();
                boolean hasLeftRecursion = this.NonterminalHasLeftRecursion(key, matchingRules);
                
                if(hasLeftRecursion)
                {
                        //we create a new rule to act as the prime value for the Rule
                    Nonterminal aPrimeSymbol = new Nonterminal(key.getName() + "'");
                        Rule aPrimeEpsilonRule = new Rule(aPrimeSymbol, new ArrayList<Symbol>());
                        //aPrimeEpsilonRule.getRule().add(Symbol.EPSILON); // add epsilon rule to aPrime
                        aPrimeEpsilonRule.getRight().add(new Token("EPSILON")); // add epsilon rule to aPrime
                        ArrayList<Rule> aPrimeRules = new ArrayList<Rule>();
                        aPrimeRules.add(aPrimeEpsilonRule);
                        
                        map.put(aPrimeSymbol, aPrimeRules);
                        
                        for (Rule pr : ( (ArrayList<Rule>) matchingRules.clone())) 
                        {       // clone so we can edit matchingRules
                        
                                Symbol startingSymbol = pr.getRight().get(0);
                        
                                if(startingSymbol.equals(key)) //this is a rule that commits left recursion
                                {
                                        matchingRules.remove(pr); // remove the offending production rule
                                        pr.getRight().remove(0); //remove the recursive call
                                        ArrayList<Symbol> newRule = new ArrayList<Symbol>();
                        
                                        newRule.addAll(pr.getRight());
                                        newRule.add(aPrimeSymbol);
                                        Rule newPrimeRule = new Rule(aPrimeSymbol, newRule );
                                        aPrimeRules.add(newPrimeRule);
                                }
                                else // the rule does not have left recursion 
                                {
                                        matchingRules.remove(pr);
                                        
                                        ArrayList<Symbol> newRule = new ArrayList<Symbol>();
                                
                                        newRule.addAll(pr.getRight());
                                        newRule.add(aPrimeSymbol);
                                        Rule newARule = new Rule(key, newRule);
                                        matchingRules.add(newARule);
                                }
                        }
                }
        }
                return map;
	}
	
    ///Hashes the rules on the basis of the nonterminal of the rule
    private HashMap<Nonterminal, ArrayList<Rule>> hashRulesByNonterminal(ArrayList<Rule> prRules)
    {
        HashMap<Nonterminal, ArrayList<Rule>> hashed = new HashMap<Nonterminal, ArrayList<Rule>>();
        Nonterminal nontermSym;
        int n=0;
        
        while(n<prRules.size())
        {
            nontermSym = prRules.get(n).getLeft();
            ArrayList<Rule> newRules = new ArrayList<Rule>();      //list of production rules that belong to the same nonterminal
            while(prRules.get(n).getLeft().getName().equals(nontermSym.getName()))
            {
            	//System.out.println(prRules.get(n));
                newRules.add(prRules.get(n));
                n++; 
                if(n>= prRules.size())
                {
                    break;
                }
            }
            hashed.put(nontermSym, newRules);
        }
        return hashed;
    }
    
	private boolean NonterminalHasLeftRecursion(Nonterminal key, ArrayList<Rule> matchingRules)
    {
        boolean hasLeftRecursion = false;
                for(Rule pr : matchingRules)//clone so there's no iterator mishap
                {
                    //System.out.println(pr);
                	ArrayList<Symbol> temp = pr.getRight();
                	if(temp.size() != 0) {
	                    Symbol startingSymbol = temp.get(0);
	                    if(startingSymbol.equals(key)) //this means that we are dealing with a rule that has Left
	                    {                                                                                           //recursion     
	                        hasLeftRecursion = true;                        
	                    }
                	}
                }
                return hasLeftRecursion;
    }
    
	//Takes a grammar rule in the for of a string as a parameter and produces/returns a list of production rules
    public ArrayList<Rule> getRules(String grammarRule)
    {
        ArrayList<Rule> productions = new ArrayList<Rule>();
        
        int index = grammarRule.indexOf(":");
        String leftSide = grammarRule.substring(0, index);
        Nonterminal nonTerm = new Nonterminal(leftSide.trim());  //get the nonterminal on left side of the arrow
        
        String rightSide = grammarRule.substring(index+1, grammarRule.length());
        //grabs the sections of the rule bordered by "|" and takes those symbols to create a production rule
        //Example grammar rule = <S> : <T> d | b | c  the first production rule made will be <S> -> <T> d
        //gScan.useDelimiter("\\|");
        //System.out.println(rightSide);
        String[] indvRules = rightSide.split("\\|", -1);
        for (String s : indvRules)
        {
           //System.out.println(s);
           String rightSyms = s.trim();
           ArrayList<Symbol> rSideSyms = new ArrayList<Symbol>();
           // special case: the rule is blank space. This means epsilon
           if (rightSyms.equals(""))
           {
               rSideSyms.add(new Token("EPSILON"));
           }
           // otherwise, the rule is broken down into its individual symbols
           else
           {
               Scanner symScan = new Scanner(rightSyms);
               //traverses symbols
               while(symScan.hasNext())
               {
                   String sym = symScan.next().trim();
                   //System.out.println("sym = " + sym);
                   //checks to see if the symbol is a nonterminal or a terminal then adds
                   //that symbol to the right side of the production rule
                   for(Token t : grammarTokens)
                   {
                	   //System.out.println("Comparing " + t + " to " + sym);
                	   if(t.equals(new Token(sym,1)))
                	   {
                		   //System.out.println("FUCK YEAH A TOKEN");
                		   rSideSyms.add(new Token(sym,1));
                		   break;
                	   }
                   }
                   for(Nonterminal t : grammarNonterminals)
                   {
                	   if(t.equals(new Nonterminal(sym)))
                	   {
                		   rSideSyms.add(new Nonterminal(sym));
                		   break;
                	   }
                   }
                   /*
                   if(grammarTokens.contains((new Token(sym, 1))))
                   {
                	   System.out.println("token");
                	   rSideSyms.add(new Token(sym));
                   }
                   else if(grammarNonterminals.contains((new Nonterminal(sym))))
                   {
                	   System.out.println("nonterminal");
                	   rSideSyms.add(new Nonterminal(sym));
                   }
                   */
               }
           }
           productions.add(new Rule(nonTerm,rSideSyms));
        }
        //System.out.println("getProdRules(" + grammarRule + ") = " + productions);
        return productions;
    }

    private ArrayList<Rule> getMaximalCommonPrefixRules(ArrayList<Rule> rules,
            ArrayList<Symbol> cp){
    
    if(rules.size() == 1 || rules.size() == 0){
            return new ArrayList<Rule>(); // if there can't be matches, return an empty list
    }       
    
    ArrayList<ArrayList<Symbol>> foundPrefix = new ArrayList<ArrayList<Symbol>>();
    for(Rule outer : rules){
            for(Rule inner : rules){              
                    ArrayList<Symbol> commonPrefix = new ArrayList<Symbol>();
                    getCommonPrefix(commonPrefix, outer.getRight(), inner.getRight());
                    if(!containsPrefix(foundPrefix, commonPrefix)){
                            if(commonPrefix.size() > 0){                            
                                    foundPrefix.add(commonPrefix);
                            }
                    }
            }
    }
    
    ArrayList<Symbol> maxPrefix = prefixOfMaximalLength(foundPrefix);
    if(maxPrefix != null){
            cp.addAll(maxPrefix);
    }
    return getRulesStartingWith(maxPrefix, rules);
}

    private ArrayList<Rule> getRulesStartingWith(ArrayList<Symbol> prefix, ArrayList<Rule> rules){
        ArrayList<Rule> retColl = new ArrayList<Rule>();
        if(prefix != null){
                for(Rule pr : rules){
                        boolean mismatch = false;
                        if(pr.getRight().size() >= prefix.size()){
                                for(int i=0; i < prefix.size() - 1; i++){
                                        if(!prefix.get(i).equals(pr.getRight().get(i))){
                                                mismatch = true;
                                                break;
                                        }
                                }
                                if(!mismatch){
                                        retColl.add(pr);
                                }
                        }
                }
        }
        return retColl;
    }
    
    private ArrayList<Symbol> prefixOfMaximalLength(ArrayList<ArrayList<Symbol>> prefixes){
        int maxSize = 0;
        ArrayList<Symbol> maxP = null;
        for(ArrayList<Symbol> p : prefixes){
                if(maxSize < p.size()){
                        maxSize = p.size();
                        maxP = p;
                }
        }
        
        return maxP;
    }
    
    private boolean containsPrefix(ArrayList<ArrayList<Symbol>> prefixes, ArrayList<Symbol> p){
        for(ArrayList<Symbol> containedPre : prefixes){
                if(p.equals(containedPre)){
                        return true;
                }
        }
        
        return false;
    }
    
    private void getCommonPrefix(ArrayList<Symbol> commonPrefix, ArrayList<Symbol> rule1, ArrayList<Symbol> rule2){
        
        if(rule1.size() == 0 || rule2.size() == 0 || rule1.equals(rule2) ){
                return;
        }
        
        if( rule1.get(0).equals(rule2.get(0))){
                ArrayList<Symbol> r1Clone = new ArrayList<Symbol>(rule1);
                ArrayList<Symbol> r2Clone = new ArrayList<Symbol>(rule2);
                r1Clone.remove(0);
                r2Clone.remove(0);
                commonPrefix.add(rule1.get(0));
            getCommonPrefix(commonPrefix, r1Clone, r2Clone);
        }
    }
    
}
