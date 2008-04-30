/* JayFX - A Fact Extractor Plug-in for Eclipse
 * Copyright (C) 2006  McGill University (http://www.cs.mcgill.ca/~swevo/jayfx)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * $Revision: 1.3 $
 */

package ca.mcgill.cs.swevo.jayfx.test;

import ca.mcgill.cs.swevo.jayfx.model.Relation;
import junit.framework.TestCase;

public class TestRelation extends TestCase {

	private Relation[] aDirectRelations;
	private Relation[] aIndirectRelations;
	
	
	public void setUp()
	{
		aDirectRelations = Relation.getAllRelations(true);
		aIndirectRelations = Relation.getAllRelations(false);
	}
	
	/**
	 * Check if there are equal number of direct and indirection relations.
	 *
	 */
	public void test1()
	{
		assertEquals( aDirectRelations.length, aIndirectRelations.length );
	}
	
	/**
	 * Check if the inverse of a direction relation is equal to 
	 * its copy in the indirection relation collections.
	 *
	 */
	public void test2()
	{
		for( int i = 0; i<aDirectRelations.length; i++ )
		{
			assertEquals( aDirectRelations[i].getInverseRelation(), aIndirectRelations[i] );
		}
	}
}
