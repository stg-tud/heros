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
package heros.ide.edgefunc.uppertype;

import heros.ide.edgefunc.AbstractFactory;
import heros.ide.edgefunc.ChainableEdgeFunction;
import heros.ide.edgefunc.EdgeFunction;

public class UpperBoundFunction<T extends Type<T>> extends ChainableEdgeFunction<T> {

	private T type;

	public UpperBoundFunction(T type, AbstractFactory<T> factory, ChainableEdgeFunction<T> chainedFunction) {
		super(factory, chainedFunction);
		this.type = type;
	}

	@Override
	public EdgeFunction<T> chain(ChainableEdgeFunction<T> f) {
		return new UpperBoundFunction<T>(type, factory, f);
	}

	@Override
	protected T _computeTarget(T source) {
		return source.meet(type);
	}

	@Override
	protected boolean mayThisReturnTop() {
		return true;
	}

	@Override
	protected EdgeFunction<T> _composeWith(ChainableEdgeFunction<T> chainableFunction) {
		if(chainableFunction instanceof UpperBoundFunction) {
			T otherType = ((UpperBoundFunction<T>) chainableFunction).type;
			T meet = otherType.meet(type);
			if(meet.equals(type))
				return this;
			else if(meet.equals(otherType))
				return chainableFunction.chainIfNotNull(chainedFunction);
			else
				return factory.allTop();
		}
		if(chainableFunction instanceof SetTypeFunction) {
			T setType = ((SetTypeFunction<T>) chainableFunction).getType();
			if(setType.meet(type).equals(setType))
				return chainableFunction.chainIfNotNull(chainedFunction);
			else
				return factory.allTop();
		}
		if(chainableFunction instanceof PopFunction && chainedFunction instanceof PushFunction)
			return chainedFunction.chainedFunction();
		
		return chainableFunction.chain(this);
	}

	@Override
	public String toString() {
		return "upperBound("+type+")"+super.toString();
	}

	public T getType() {
		return type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		UpperBoundFunction other = (UpperBoundFunction) obj;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
}
