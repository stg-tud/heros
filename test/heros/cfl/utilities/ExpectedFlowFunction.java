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
package heros.cfl.utilities;

import heros.cfl.solver.FlowFunction;
import heros.utilities.TestFact;

import com.google.common.base.Joiner;

public abstract class ExpectedFlowFunction<Fact> {

	public final Fact source;
	public final Fact[] targets;
	public Edge edge;
	public int times;

	public ExpectedFlowFunction(int times, Fact source, Fact... targets) {
		this.times = times;
		this.source = source;
		this.targets = targets;
	}

	@Override
	public String toString() {
		return String.format("%s: %s -> {%s}", edge, source, Joiner.on(",").join(targets));
	}
	
	public abstract String transformerString();

	public abstract FlowFunction.ConstrainedFact<TestFact> apply(TestFact target);
	
}