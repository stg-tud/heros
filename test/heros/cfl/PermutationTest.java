/*******************************************************************************
 * Copyright (c) 2016 Johannes Lerch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Johannes Lerch - initial API and implementation
 ******************************************************************************/
package heros.cfl;

import static org.junit.Assert.assertEquals;
import heros.cfl.Rule.Traversal;
import heros.solver.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class PermutationTest {

	private int reachableNonTerminals = 0;
	private ArrayList<NonTerminal> nonTerminals;
	private ArrayList<NonTerminal> nonTerminalPrimes;
	private ArrayList<Terminal> terminals;
	private Random random = new Random(1);
	private Set<Pair<NonTerminal, Rule>> expectation;
	private Set<Pair<NonTerminal, Rule>> actual;

	public PermutationTest(ArrayList<NonTerminal> nonTerminals, ArrayList<NonTerminal> nonTerminalPrimes, ArrayList<Terminal> terminals) {
		this.nonTerminals = nonTerminals;
		this.nonTerminalPrimes = nonTerminalPrimes;
		this.terminals = terminals;
	}
	
	public void clean() {
		for(NonTerminal nt : nonTerminals)
			nt.removeAllRules();
		for(NonTerminal nt : nonTerminalPrimes) {
			nt.removeAllRules();
			nt.addRule(new ConstantRule());
		}
	}
	
	public List<Pair<NonTerminal, Rule>> setup(int rules) {
		reachableNonTerminals = 0;
		LinkedList<Pair<NonTerminal, Rule>> result = Lists.newLinkedList();
		NonTerminal root = getNonTerminal();
		ConstantRule rootRule = new ConstantRule(createTerminalsOrEmpty());
		result.add(new Pair<NonTerminal, Rule>(root, rootRule));
		root.addRule(rootRule);
		
		for(int i=0; i<rules; i++) {
			NonTerminal nonTerminal = getPotentiallyNewNonTerminal();
			Rule newRule = createRule(0);
			result.add(new Pair<NonTerminal, Rule>(nonTerminal, newRule));
			nonTerminal.addRule(newRule);
		}
		return result;
	}

	private Rule createRule(int minimumRuleType) {
		switch(random.nextInt(7-minimumRuleType) + minimumRuleType) {
		case 0: return new ConstantRule(createTerminalsOrEmpty());
		case 1: return new ConstantRule(createTerminals());
		case 2:
		case 3:
		case 4: return new RegularRule(getNonTerminal(), createTerminalsOrEmpty());
		case 5: return new ContextFreeRule(createTerminals(), getNonTerminal(), createTerminalsOrEmpty());
		case 6: return new NonLinearRule(createRule(1), createRule(2));
		}
		throw new IllegalStateException();
	}
	
	private NonTerminal getPotentiallyNewNonTerminal() {
		int index = random.nextInt(Math.min(reachableNonTerminals+1, nonTerminals.size()));
		reachableNonTerminals = Math.max(reachableNonTerminals, index);
		return nonTerminals.get(index);
	}

	private NonTerminal getNonTerminal() {
		return nonTerminals.get(random.nextInt(reachableNonTerminals+1));
	}

	private Terminal[] createTerminalsOrEmpty() {
		if(random.nextBoolean())
			return new Terminal[0];
		else
			return new Terminal[] {terminals.get(random.nextInt(terminals.size()))};
	}

	private Terminal[] createTerminals() {
		return new Terminal[] {terminals.get(random.nextInt(terminals.size()))};
	}

	public Collection<NonTerminal> getNonTerminals() {
		return nonTerminals;
	}

	private Set<Pair<NonTerminal, Rule>> collect() {
		HashSet<Pair<NonTerminal, Rule>> result = Sets.newHashSet();
		for(NonTerminal nt : nonTerminals) {
			for(Rule rule : nt.getRules()) {
				result.add(new Pair<NonTerminal, Rule>(nt, rule));
			}
		}
		for(NonTerminal nt : nonTerminalPrimes) {
			for(Rule rule : nt.getRules()) {
				result.add(new Pair<NonTerminal, Rule>(nt, rule));
			}
		}	
		return result;
	}
	
	public void collectExpectation() {
		expectation = collect();
	}

	public void collectActual() {
		actual = collect();
	}

	public void compareAndAssert(List<Pair<NonTerminal, Rule>> permutation) {
		assertEquals("Rule permutation:\n"+toString(permutation)
				+"\nExpected: \n"+toString(expectation)
				+"\nActual:\n"+toString(actual), traversal(expectation), traversal(actual));
	}
	
	private Set<Pair<NonTerminal, List<Object>>> traversal(Set<Pair<NonTerminal, Rule>> rules) {
		Set<Pair<NonTerminal, List<Object>>> out = Sets.newHashSet();
		for(Pair<NonTerminal, Rule> r : rules) {
			final List<Object> result = Lists.newLinkedList();
			r.getO2().traverse(new Traversal() {
				@Override
				public void nonTerminal(NonTerminal nt) {
					result.add(nt);
				}

				@Override
				public void terminal(Terminal t) {
					result.add(t);
				}
			});
		}
		return out;
	}

	public String toString(Collection<Pair<NonTerminal, Rule>> permutation) {
		Multimap<NonTerminal, Rule> result = HashMultimap.create();
		for(Pair<NonTerminal, Rule> pair : permutation) {
			result.put(pair.getO1(), pair.getO2());
		}
		String output = "";
		for(NonTerminal nt : result.keySet()) {
			output += nt +" → ";
			output += Joiner.on(" | ").join(result.get(nt));
			output += "\n";
		}
		return output;
	}

}