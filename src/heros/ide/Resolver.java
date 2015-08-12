/*******************************************************************************
 * Copyright (c) 2015 Johannes Lerch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Johannes Lerch - initial API and implementation
 ******************************************************************************/
package heros.ide;

import heros.ide.edgefunc.EdgeFunction;
import heros.solver.Pair;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public abstract class Resolver<Fact, Stmt, Method, Value> {

	private Set<Pair<EdgeFunction<Value>, Resolver<Fact, Stmt, Method, Value>>> resolvedUnbalanced = Sets.newHashSet();
	private List<InterestCallback<Fact, Stmt, Method, Value>> interestCallbacks = Lists.newLinkedList();
	protected PerAccessPathMethodAnalyzer<Fact, Stmt, Method, Value> analyzer;
	private Set<EdgeFunction<Value>> balancedFunctions = Sets.newHashSet();
	
	public Resolver(PerAccessPathMethodAnalyzer<Fact, Stmt, Method, Value> analyzer) {
		this.analyzer = analyzer;
	}

	public abstract void resolve(EdgeFunction<Value> edgeFunction, InterestCallback<Fact, Stmt, Method, Value> callback);
	
	public void resolvedUnbalanced(EdgeFunction<Value> edgeFunction, Resolver<Fact, Stmt, Method, Value> resolver) {
		if(!resolvedUnbalanced.add(new Pair<EdgeFunction<Value>, Resolver<Fact, Stmt, Method, Value>>(edgeFunction, resolver)))
			return;

		log("Interest given by EdgeFunction: "+edgeFunction);
		for(InterestCallback<Fact, Stmt, Method, Value> callback : Lists.newLinkedList(interestCallbacks)) {
			callback.interest(analyzer, resolver, edgeFunction);
		}
	}
	
	public boolean isResolvedUnbalanced() {
		return !resolvedUnbalanced.isEmpty();
	}
	
	protected void continueBalancedTraversal(EdgeFunction<Value> edgeFunction) {
		if(balancedFunctions.add(edgeFunction)) {
			for(InterestCallback<Fact, Stmt, Method, Value> callback : Lists.newLinkedList(interestCallbacks)) {
				callback.continueBalancedTraversal(edgeFunction);
			}
		}
	}

	protected void registerCallback(InterestCallback<Fact, Stmt, Method, Value> callback) {
		for (Pair<EdgeFunction<Value>, Resolver<Fact, Stmt, Method, Value>> pair : Lists.newLinkedList(resolvedUnbalanced)) {
			callback.interest(analyzer, pair.getO2(), pair.getO1());
		}
		log("Callback registered");
		interestCallbacks.add(callback);

		if(!balancedFunctions.isEmpty()) {
			for(EdgeFunction<Value> edgeFunction : Lists.newLinkedList(balancedFunctions)) {
				callback.continueBalancedTraversal(edgeFunction);
			}
		}
	}
	
	protected abstract void log(String message);
	
}