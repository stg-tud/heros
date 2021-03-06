/*******************************************************************************
 * Copyright (c) 2012 Eric Bodden.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Eric Bodden - initial API and implementation
 ******************************************************************************/
package heros.ide;

import java.util.Map;
import java.util.Set;

import heros.IFDSTabulationProblem;
import heros.InterproceduralCFG;
import heros.JoinLattice;
import heros.ide.edgefunc.EdgeFunction;

/**
 * Defines an IDE tabulation problem as presented in the Sagiv, Reps, Horwitz 1996 
 * (SRH96) paper. An IDE tabulation problem extends an {@link IFDSTabulationProblem}
 * by allowing additional values to be computed along flow functions: each domain value
 * of type D maps at any program point to a value of type V. The functions describe how
 * values are transformed when moving from one statement to another.
 * 
 * The problem further defines a {@link JoinLattice}, which describes how values of
 * type V are joined (merged) when multiple values are possible.
 *
 * @param <N> The type of nodes in the interprocedural control-flow graph. Typically {@link Unit}.
 * @param <D> The type of data-flow facts to be computed by the tabulation problem.
 * @param <M> The type of objects used to represent methods. Typically {@link SootMethod}.
 * @param <V> The type of values to be computed along flow edges.
 * @param <I> The type of inter-procedural control-flow graph being used.
 */
public interface IDETabulationProblem<N,D,M,V,I extends InterproceduralCFG<N,M>> {

	/**
	 * Returns the lattice describing how values of type V need to be joined.
	 */
	JoinLattice<V> joinLattice();

	/**
	 * Returns a function mapping everything to top.
	 */	
	EdgeFunction<V> allTopFunction(); 
	
	EdgeFunction<V> initialSeedEdgeFunction(N seed, D val);
	
	/**
	 * Returns a set of flow functions. Those functions are used to compute data-flow facts
	 * along the various kinds of control flows.
     *
	 * <b>NOTE:</b> this method could be called many times. Implementations of this
	 * interface should therefore cache the return value! 
	 */
	FlowFunctions<N,D,M, V> flowFunctions();
	
	/**
	 * Returns the interprocedural control-flow graph which this problem is computed over.
	 * 
	 * <b>NOTE:</b> this method could be called many times. Implementations of this
	 * interface should therefore cache the return value! 
	 */
	I interproceduralCFG();
	
	/**
	 * Returns initial seeds to be used for the analysis. This is a mapping of statements to initial analysis facts.
	 */
	Map<N,Set<D>> initialSeeds();
	
	/**
	 * This must be a data-flow fact of type {@link D}, but must <i>not</i>
	 * be part of the domain of data-flow facts. Typically this will be a
	 * singleton object of type {@link D} that is used for nothing else.
	 * It must holds that this object does not equals any object 
	 * within the domain.
	 *
	 * <b>NOTE:</b> this method could be called many times. Implementations of this
	 * interface should therefore cache the return value! 
	 */
	D zeroValue();
	
	boolean followReturnsPastSeeds();
}
