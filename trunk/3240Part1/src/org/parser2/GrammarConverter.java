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
	
	//from louden 159
	@SuppressWarnings("unchecked")
	private HashMap<Nonterminal, ArrayList<Rule>> removeLeftRecursion(ArrayList<String> inRules) {
	    ArrayList<Rule> tmpRules = new ArrayList<Rule>();
	    for(String rule : inRules )
	    {
            ArrayList<Rule> prodRules = this.getRules(rule);
            tmpRules.addAll(prodRules);
        }
        
	    
	    
        HashMap<Nonterminal, ArrayList<Rule>> ruleHash = new HashMap<Nonterminal, ArrayList<Rule>>();
        
        Nonterminal nonterm;
        int i=0;
        
        while(i<tmpRules.size())
        {
            nonterm = tmpRules.get(i).getLeft();
            ArrayList<Rule> newRules = new ArrayList<Rule>();  
            while(tmpRules.get(i).getLeft().getName().equals(nonterm.getName()))
            {
                newRules.add(tmpRules.get(i));
                i++; 
                if(i>= tmpRules.size())
                {
                    break;
                }
            }
            ruleHash.put(nonterm, newRules);
        }
        
        ArrayList<Nonterminal> indexes = new ArrayList<Nonterminal>();
        indexes.addAll(ruleHash.keySet());
        for (Nonterminal index : indexes )
        {       
            ArrayList<Rule> matchRules = ruleHash.get(index);
            boolean hasLeftRecursion = this.NonterminalHasLeftRecursion(index, matchRules);
            
            if(hasLeftRecursion)
            {
                Nonterminal aPrime = new Nonterminal(index.getName() + "'");
                Rule aPrimeERule = new Rule(aPrime, new ArrayList<Symbol>());
                aPrimeERule.getRight().add(new Token("EPSILON")); // add epsilon rule to aPrime
                ArrayList<Rule> aPrimeRules = new ArrayList<Rule>();
                aPrimeRules.add(aPrimeERule);
                
                ruleHash.put(aPrime, aPrimeRules);
                
                for (Rule lrule : ( (ArrayList<Rule>) matchRules.clone())) 
                {
                    Symbol startingSymbol = lrule.getRight().get(0);
            
                    if(startingSymbol.equals(index))
                    {
                    	//left recursion present
                        matchRules.remove(lrule); 
                        lrule.getRight().remove(0); 
                        ArrayList<Symbol> newRule = new ArrayList<Symbol>();
        
                        newRule.addAll(lrule.getRight());
                        newRule.add(aPrime);
                        Rule newPrimeRule = new Rule(aPrime, newRule );
                        aPrimeRules.add(newPrimeRule);
                    }
                    else 
                    {
                    	//no left recursion present
                        matchRules.remove(lrule);
                        
                        ArrayList<Symbol> newRule = new ArrayList<Symbol>();
                
                        newRule.addAll(lrule.getRight());
                        newRule.add(aPrime);
                        Rule newARule = new Rule(index, newRule);
                        matchRules.add(newARule);
                    }
                }
            }
        }
                return ruleHash;
	}
	
	//from louden 164
	private HashMap<Nonterminal, ArrayList<Rule>> removeCommonPrefix(
            HashMap<Nonterminal, ArrayList<Rule>> ruleHash) {

		boolean changed = true;
		while(changed){
			changed = false;
			ArrayList<Nonterminal> keys = new ArrayList<Nonterminal>();
			keys.addAll(ruleHash.keySet());
			for (Nonterminal key : keys )
			{
			    ArrayList<Rule> matchRules = ruleHash.get(key);
			    ArrayList<Symbol> commpref = new ArrayList<Symbol>();
			    ArrayList<Rule> commPrefRules = getMaximalPrefixRules(matchRules, commpref);
			    if(commPrefRules.size() > 1){ 
		            changed = true;
		            Nonterminal a = new Nonterminal(key.getName() + key.getName().hashCode());
		            
		            ArrayList<Symbol> replaceRule = new ArrayList<Symbol>();
		            replaceRule.addAll(commpref);
		            replaceRule.add(a);
		            Rule replacePR = new Rule(key, replaceRule);
		            matchRules.removeAll(commPrefRules);
		            matchRules.add(replacePR);
		            
		            ArrayList<Rule> aPR = new ArrayList<Rule>();
		            for(Rule rul : commPrefRules){
	                    ArrayList<Symbol> newARule = new ArrayList<Symbol>();
	                    for(int i = commpref.size(); i < rul.getRight().size(); i++){
	                        newARule.add(rul.getRight().get(i));
	                    }
	                    if(newARule.size() == 0){
	                        newARule.add(new Token("EPSILON"));
	                    }
	                    Rule newAPR = new Rule(a, newARule);
	                    aPR.add(newAPR);
		            }
		            ruleHash.put(a, aPR);
			    }
			}
		}
		
	    return ruleHash;
	}
    
	private boolean NonterminalHasLeftRecursion(Nonterminal index, ArrayList<Rule> matchingRules)
    {
        boolean hasLeftRecursion = false;
                for(Rule lrule : matchingRules)
                {
                	ArrayList<Symbol> tmp = lrule.getRight();
                	if(tmp.size() != 0) {
	                    Symbol startingSymbol = tmp.get(0);
	                    if(startingSymbol.equals(index))
	                    {                        
	                        hasLeftRecursion = true;                        
	                    }
                	}
                }
                return hasLeftRecursion;
    }
    
    public ArrayList<Rule> getRules(String grammarRule)
    {
        ArrayList<Rule> prodRules = new ArrayList<Rule>();
        
        int index = grammarRule.indexOf(":");
        String left = grammarRule.substring(0, index);
        Nonterminal nonT = new Nonterminal(left.trim());
        
        String right = grammarRule.substring(index+1, grammarRule.length());

        String[] allRules = right.split("\\|", -1);
        for (String s : allRules)
        {
           //System.out.println(s);
           String rightToks = s.trim();
           ArrayList<Symbol> rToks = new ArrayList<Symbol>();
           if (rightToks.equals(""))
           {
               rToks.add(new Token("EPSILON"));
           }
           else
           {
               Scanner tokScan = new Scanner(rightToks);
               while(tokScan.hasNext())
               {
                   String tok = tokScan.next().trim();
                   
                   for(Token t : grammarTokens)
                   {
                	   if(t.equals(new Token(tok,1)))
                	   {
                		   rToks.add(new Token(tok,1));
                		   break;
                	   }
                   }
                   for(Nonterminal t : grammarNonterminals)
                   {
                	   if(t.equals(new Nonterminal(tok)))
                	   {
                		   rToks.add(new Nonterminal(tok));
                		   break;
                	   }
                   }

               }
           }
           prodRules.add(new Rule(nonT,rToks));
        }
        return prodRules;
    }

    private ArrayList<Rule> getMaximalPrefixRules(ArrayList<Rule> rules,
            ArrayList<Symbol> cp){
    
	    if(rules.size() == 1 || rules.size() == 0){
            return new ArrayList<Rule>();
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
        ArrayList<Rule> ret = new ArrayList<Rule>();
        if(prefix != null){
            for(Rule rule : rules){
                boolean mismatch = false;
                if(rule.getRight().size() >= prefix.size()){
                    for(int i=0; i < prefix.size() - 1; i++){
                        if(!prefix.get(i).equals(rule.getRight().get(i))){
                            mismatch = true;
                            break;
                        }
                    }
                    if(!mismatch){
                        ret.add(pr);
                    }
                }
            }
        }
        return ret;
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
