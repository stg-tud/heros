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
package heros.ide.edgefunc.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Set;

import heros.JoinLattice;
import heros.ide.edgefunc.ChainableEdgeFunction;
import heros.ide.edgefunc.EdgeFunction;
import heros.ide.edgefunc.type.Factory;
import heros.ide.edgefunc.type.Type;

import org.junit.Test;

import com.google.common.collect.Sets;

public class FunctionCompositionTest {

	private Factory<TestType> factory = new Factory<TestType>(mock(JoinLattice.class));
	
	private TestType superType = new TestType("superType");
	private TestType type = new TestType("type", superType);
	private TestType subType = new TestType("subType", type);
	
	private TypeBoundary<TestType> boundary = tb(subType, type);
	
	private static TypeBoundary<TestType> tb(TestType lower, TestType upper) {
		return new TypeBoundary<TestType>(lower, upper);
	}
	
	@Test(expected=IllegalStateException.class)
	public void anyBeforeAny() {
		factory.any().composeWith(factory.any());
	}
		
	@Test
	public void anyBeforeEmpty() {
		EdgeFunction<TypeBoundary<TestType>> actual = factory.any().composeWith(factory.empty());
		assertEquals(factory.empty(), actual);
	}
	
	@Test
	public void anyBeforePop() {
		EdgeFunction<TypeBoundary<TestType>> actual = factory.any().composeWith(factory.pop());
		assertEquals(factory.any(), actual);
	}
	
	@Test
	public void anyBeforePush() {
		assertNoSimplification(factory.any(), factory.push());
	}
	
	@Test
	public void anyBeforeUpperBound() {
		assertEquals(factory.anyOrUpperBound(boundary), factory.any().composeWith(factory.upperBound(boundary)));
	}
	
	@Test(expected=IllegalStateException.class)
	public void anyOrUpperBoundBeforeAny() {
		factory.anyOrUpperBound(boundary).composeWith(factory.any());
	}
	
	@Test
	public void anyOrUpperBoundBeforeEmpty() {
		EdgeFunction<TypeBoundary<TestType>> actual = factory.anyOrUpperBound(boundary).composeWith(factory.empty());
		assertEquals(factory.empty(), actual);
	}
	
	@Test
	public void anyOrUpperBoundBeforePop() {
		EdgeFunction<TypeBoundary<TestType>> actual = factory.anyOrUpperBound(boundary).composeWith(factory.pop());
		assertEquals(factory.any(), actual);
	}
	
	@Test
	public void anyOrUpperBoundBeforePush() {
		assertNoSimplification(factory.anyOrUpperBound(boundary), factory.push());
	}
	
	@Test
	public void anyOrUpperBoundBeforeUpperBoundMatch() {
		assertEquals(factory.anyOrUpperBound(tb(type, type)), factory.anyOrUpperBound(tb(type, superType)).composeWith(factory.upperBound(tb(subType, type))));
		assertEquals(factory.anyOrUpperBound(tb(type, type)), factory.anyOrUpperBound(tb(subType, type)).composeWith(factory.upperBound(tb(type, superType))));
	}
	
	@Test
	public void anyOrUpperBoundBeforeUpperBoundMismatch() {
		assertEquals(factory.allTop(), factory.anyOrUpperBound(tb(type, superType)).composeWith(factory.upperBound(tb(subType, subType))));
		assertEquals(factory.allTop(), factory.anyOrUpperBound(tb(subType, subType)).composeWith(factory.upperBound(tb(type, superType))));
	}
	
	@Test
	public void emptyBeforeAny() {
		assertNoSimplificationKeepOrigin(factory.empty(), factory.any());
	}
	
	@Test
	public void emptyBeforeEmpty() {
		EdgeFunction<TypeBoundary<TestType>> actual = factory.empty().composeWith(factory.empty());
		assertEquals(factory.empty(), actual);
	}
	
	@Test
	public void emptyBeforePop() {
		EdgeFunction<TypeBoundary<TestType>> actual = factory.empty().composeWith(factory.pop());
		assertEquals(factory.allTop(), actual);
	}
	
	@Test
	public void emptyBeforePush() {
		assertNoSimplificationKeepOrigin(factory.empty(), factory.push());
	}
	
	@Test
	public void emptyBeforeUpperBound() {
		assertNoSimplification(factory.empty(), factory.upperBound(boundary));
	}
	
	@Test
	public void initBeforeEmpty() {
		assertEquals(factory.init(), factory.init().composeWith(factory.empty()));
	}
	
	@Test
	public void initBeforePop() {
		assertEquals(factory.allTop(), factory.init().composeWith(factory.pop()));
	}
	
	@Test
	public void pushBeforeEmpty() {
		assertEquals(factory.allTop(), factory.push().composeWith(factory.empty()));
	}
	
	@Test
	public void pushBeforePop() {
		assertEquals(factory.id(), factory.push().composeWith(factory.pop()));
	}
	
	@Test
	public void upperBoundBeforeEmpty() {
		assertNoSimplificationKeepOrigin(factory.upperBound(boundary), factory.empty());
	}
	
	@Test
	public void upperBoundBeforePop() {
		assertNoSimplificationKeepOrigin(factory.upperBound(boundary), factory.pop());
	}
	
	@Test
	public void upperBoundBeforePush() {
		assertNoSimplificationKeepOrigin(factory.upperBound(boundary), factory.push());
	}
	
	@Test
	public void upperBoundBeforeUpperBoundMatch() {
		assertEquals(factory.upperBound(tb(subType, type)), factory.upperBound(tb(subType, type)).composeWith(factory.upperBound(tb(subType, superType))));
		assertEquals(factory.upperBound(tb(type, type)), factory.upperBound(tb(type, superType)).composeWith(factory.upperBound(tb(subType, type))));
	}
	
	@Test
	public void upperBoundBeforeUpperBoundMismatch() {
		assertEquals(factory.allTop(), factory.upperBound(tb(subType, subType)).composeWith(factory.upperBound(tb(type, superType))));
		assertEquals(factory.allTop(), factory.upperBound(tb(type, superType)).composeWith(factory.upperBound(tb(subType, subType))));
	}
	
	@Test
	public void pushUpperBoundPop() {
		assertEquals(factory.id(), factory.push().composeWith(factory.upperBound(boundary)).composeWith(factory.pop()));
	}
	
	@Test
	public void anyUpperBoundPop() {
		assertEquals(factory.any(), factory.any().composeWith(factory.upperBound(boundary)).composeWith(factory.pop()));
	}

	private static void assertNoSimplification(EdgeFunction<TypeBoundary<TestType>> first, EdgeFunction<TypeBoundary<TestType>> second) {
		ChainableEdgeFunction<TypeBoundary<TestType>> firstChainable = (ChainableEdgeFunction) first;
		ChainableEdgeFunction<TypeBoundary<TestType>> secondChainable = (ChainableEdgeFunction) second;
		EdgeFunction<TypeBoundary<TestType>> actual = firstChainable.composeWith(secondChainable);
		
		assertTrue(actual instanceof ChainableEdgeFunction);
		ChainableEdgeFunction<TypeBoundary<TestType>> chainedActual = (ChainableEdgeFunction<TypeBoundary<TestType>>) actual;
		assertEquals(second.getClass(), chainedActual.getClass());
		assertTrue(chainedActual.chainedFunction() instanceof ChainableEdgeFunction);
	}
	
	private static void assertNoSimplificationKeepOrigin(EdgeFunction<TypeBoundary<TestType>> first, EdgeFunction<TypeBoundary<TestType>> second) {
		ChainableEdgeFunction<TypeBoundary<TestType>> firstChainable = (ChainableEdgeFunction) first;
		ChainableEdgeFunction<TypeBoundary<TestType>> secondChainable = (ChainableEdgeFunction) second;
		ChainableEdgeFunction<TypeBoundary<TestType>> origin = mock(ChainableEdgeFunction.class);
		EdgeFunction<TypeBoundary<TestType>> actual = firstChainable.chain(origin).composeWith(secondChainable);
		
		assertTrue(actual instanceof ChainableEdgeFunction);
		ChainableEdgeFunction<TypeBoundary<TestType>> chainedActual = (ChainableEdgeFunction<TypeBoundary<TestType>>) actual;
		assertEquals(second.getClass(), chainedActual.getClass());
		assertTrue(chainedActual.chainedFunction() instanceof ChainableEdgeFunction);
		ChainableEdgeFunction<TypeBoundary<TestType>> nested = (ChainableEdgeFunction)chainedActual.chainedFunction();
		assertEquals(first.getClass(), nested.getClass());
		assertEquals(origin, nested.chainedFunction());
	}
	
	private static class TestType implements Type<TestType> {

		private TestType superType;
		private String identifier;
		
		public TestType(String identifier) {
			this.identifier = identifier;
			this.superType = null;
		}
		
		public TestType(String identifier, TestType superType) {
			this.identifier = identifier;
			this.superType = superType;
		}

		@Override
		public TestType join(TestType j) {
			Set<TestType> superTypes = Sets.newHashSet();
			{
				TestType current = this;
				while(current != null){
					superTypes.add(current);
					current = current.superType;
				}
			}
			TestType current = j;
			while(current!=null) {
				if(superTypes.contains(current))
					return current;
				current = current.superType;
			}
			throw new IllegalStateException(this+" and "+j+" do not share a super type.");
		}

		@Override
		public TestType meet(TestType m) {
			{
				TestType current = this;
				do {
					if(current == m)
						return this;
					current = current.superType;
				}while(current!=null);
			}
			
			{
				TestType current = m;
				do {
					if(current == this)
						return m;
					current = current.superType;
				}while(current!=null);
			}
			
			return new TestType("none");
		}
		
		@Override
		public String toString() {
			return identifier;
		}
	}
}
