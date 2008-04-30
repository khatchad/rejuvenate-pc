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

import java.util.Set;

import ca.mcgill.cs.swevo.jayfx.ElementNotFoundException;
import ca.mcgill.cs.swevo.jayfx.ProgramDatabase;
import ca.mcgill.cs.swevo.jayfx.model.FlyweightElementFactory;
import ca.mcgill.cs.swevo.jayfx.model.ICategories;
import ca.mcgill.cs.swevo.jayfx.model.IElement;
import ca.mcgill.cs.swevo.jayfx.model.Relation;
import junit.framework.TestCase;

public class TestProgramDatabase extends TestCase
{
    private IElement aClass1;
    private IElement aClass2;
    private IElement aField1;
    private IElement aField2;
    private IElement aMethod1;
    private IElement aMethod2;
    private ProgramDatabase aDB;
    
    protected void setUp() throws Exception 
    {
        aClass1 = FlyweightElementFactory.getElement( ICategories.CLASS, "a.b.c.Class1", null);
        aClass2 = FlyweightElementFactory.getElement( ICategories.CLASS, "a.b.c.Class2", null);
        aField1 = FlyweightElementFactory.getElement( ICategories.FIELD, "a.b.c.Class1.aField1", null);
        aField2 = FlyweightElementFactory.getElement( ICategories.FIELD, "a.b.c.Class2.aField2", null);
        aMethod1 = FlyweightElementFactory.getElement( ICategories.METHOD, "a.b.c.Class1.method1()", null);
        aMethod2 = FlyweightElementFactory.getElement( ICategories.METHOD, "a.b.c.Class2.method2()", null);
        aDB = new ProgramDatabase();
    }

    protected void tearDown() throws Exception 
    {
    }
    
    public void testAddElement()
    {
        aDB.addElement( aClass1, 0 );
        assertTrue( aDB.contains( aClass1 ));
        aDB.addElement( aClass2, 0 );
        assertTrue( aDB.contains( aClass2 ));
        aDB.addRelation( aClass1, Relation.DECLARES_TYPE, aClass2 );
        aDB.addElement( aClass1, 0 );
        Set lSet = aDB.getRange( aClass1, Relation.DECLARES_TYPE );
        assertTrue( lSet.size() == 1 );
    }
    
    public void testAddRelation()
    {
    	try
		{
    		aDB.addRelation( aClass1, Relation.CALLS, aMethod1 );
    		fail( "Expected ElementNotFoundException");
		}
    	catch( ElementNotFoundException pException )
		{}
    	aDB.addElement( aClass1, 0 );
    	try
		{
    		aDB.addRelation( aClass1, Relation.CALLS, aMethod1 );
    		fail( "Expected ElementNotFoundException");
		}
    	catch( ElementNotFoundException pException )
		{}
    	aDB.addElement( aMethod1, 0 );
    	aDB.addRelation( aClass1, Relation.CALLS, aMethod1 );
    	Set lSet = aDB.getRange( aClass1, Relation.CALLS );
    	assertTrue( lSet.size() == 1);
    	assertTrue( lSet.contains( aMethod1 ));
    	aDB.addElement( aMethod2, 0 );
    	aDB.addRelation( aClass1, Relation.CALLS, aMethod2 );
    	lSet = aDB.getRange( aClass1, Relation.CALLS );
    	assertTrue( lSet.size() == 2 );
    	assertTrue( lSet.contains( aMethod1 ));
    	assertTrue( lSet.contains( aMethod2 ));
    }
    
    public void testAddRelationAndTranspose()
    {
    	try
		{
    		aDB.addRelationAndTranspose( aClass1, Relation.CALLS, aMethod1 );
    		fail( "Expected ElementNotFoundException");
		}
    	catch( ElementNotFoundException pException )
		{}
    	aDB.addElement( aClass1, 0 );
    	try
		{
    		aDB.addRelation( aClass1, Relation.CALLS, aMethod1 );
    		fail( "Expected ElementNotFoundException");
		}
    	catch( ElementNotFoundException pException )
		{}
    	aDB.addElement( aMethod1, 0 );
    	aDB.addRelationAndTranspose( aClass1, Relation.CALLS, aMethod1 );
    	Set lSet = aDB.getRange( aClass1, Relation.CALLS );
    	assertTrue( lSet.size() == 1);
    	assertTrue( lSet.contains( aMethod1 ));
    	lSet = aDB.getRange( aMethod1, Relation.CALLS );
    	assertTrue( lSet.size() == 0 );
    	lSet = aDB.getRange( aMethod1, Relation.T_CALLS );
    	assertTrue( lSet.size() == 1 );
    	assertTrue( lSet.contains( aClass1 ));
    	aDB.addElement( aMethod2, 0 );
    	aDB.addRelationAndTranspose( aMethod2, Relation.T_CALLS, aClass1 );
    	lSet = aDB.getRange( aClass1, Relation.CALLS );
    	assertTrue( lSet.size() == 2 );
    	assertTrue( lSet.contains( aMethod1 ));
    	assertTrue( lSet.contains( aMethod2 ));
    	lSet = aDB.getRange( aMethod2, Relation.T_CALLS );
    	assertTrue( lSet.size() == 1 );
    	assertTrue( lSet.contains( aClass1 ));
    }
    
    public void testContains()
    {
    	aDB.addElement( aClass1, 0 );
    	aDB.addElement( aClass2, 0 );
    	aDB.contains( aClass1 );
    	aDB.contains( aClass2 );
    }
    
    public void testCopyRelations()
    {
    	// Invalid case
    	try
		{
    		aDB.copyRelations( aClass1, aClass2 );
    		fail( "Expected ElementNotFoundException");
		}
    	catch( ElementNotFoundException pException )
		{}
    	aDB.addElement( aClass1, 0 );
    	try
		{
    		aDB.addRelation( aClass1, Relation.CALLS, aMethod1 );
    		fail( "Expected ElementNotFoundException");
		}
    	catch( ElementNotFoundException pException )
		{}
    	// Empty case
    	aDB.addElement( aClass2, 0 );
    	aDB.copyRelations( aClass1, aClass2 );
    	// Unit case
    	aDB.addElement( aMethod1, 0 );
    	aDB.addElement( aField1, 0 );
    	aDB.addRelationAndTranspose( aClass1, Relation.DECLARES_FIELD, aField1 );
    	aDB.addRelationAndTranspose( aClass1, Relation.CALLS, aMethod1 );
    	aDB.copyRelations( aClass1, aClass2 );
    	Set lSet = aDB.getRange( aClass1, Relation.DECLARES_FIELD );
    	assertTrue( lSet.size() == 1 );
    	assertTrue( lSet.contains( aField1 ));
    	lSet = aDB.getRange( aClass1, Relation.CALLS );
    	assertTrue( lSet.size() == 1 );
    	assertTrue( lSet.contains( aMethod1 ));
    	lSet = aDB.getRange( aField1, Relation.T_DECLARES );
    	assertTrue( lSet.size() == 2 );
    	assertTrue( lSet.contains( aClass1 ));
    	assertTrue( lSet.contains( aClass2 ));
    	lSet = aDB.getRange( aMethod1, Relation.T_CALLS );
    	assertTrue( lSet.size() == 2 );
    	assertTrue( lSet.contains( aClass1 ));
    	assertTrue( lSet.contains( aClass2 ));
    	lSet = aDB.getRange( aClass2, Relation.DECLARES_FIELD );
    	assertTrue( lSet.size() == 1 );
    	assertTrue( lSet.contains( aField1 ));
    	lSet = aDB.getRange( aClass2, Relation.CALLS );
    	assertTrue( lSet.size() == 1 );
    	assertTrue( lSet.contains( aMethod1 ));
    	// Adding on to existing relations
    	aDB.addRelationAndTranspose( aField1, Relation.T_ACCESSES, aMethod1 );
    	aDB.copyRelations( aField1, aClass2 );
    	lSet = aDB.getRange( aMethod1, Relation.ACCESSES );
    	assertTrue( lSet.size() == 2 );
    	assertTrue( lSet.contains( aField1 ));
    	assertTrue( lSet.contains( aClass2 ));
    	lSet = aDB.getRange( aClass2, Relation.T_ACCESSES );
    	assertTrue( lSet.size() == 1 );
    	assertTrue( lSet.contains( aMethod1 ));
    }
    
    public void testGetAllElements()
    {
    	Set lSet = aDB.getAllElements();
    	assertTrue( lSet.size() == 0 );
    	aDB.addElement( aClass1, 0 );
    	lSet = aDB.getAllElements();
    	assertTrue( lSet.size() == 1 );
    	assertTrue( lSet.contains( aClass1 ));
    	aDB.addElement( aClass2, 0 );
    	lSet = aDB.getAllElements();
    	assertTrue( lSet.size() == 2 );
    	assertTrue( lSet.contains( aClass1 ));
    	assertTrue( lSet.contains( aClass2 ));
    }
    
    public void testGetRange()
    {
    	// Invalid case
    	try
		{
    		aDB.getRange( aClass1, Relation.DECLARES_METHOD );
    		fail( "Expected ElementNotFoundException");
		}
    	catch( ElementNotFoundException pException )
		{}
    	// Empty Case
    	aDB.addElement( aClass1, 0 );
    	Set lSet = aDB.getRange( aClass1, Relation.DECLARES_METHOD );
    	assertTrue( lSet.size() == 0 );
    	aDB.addElement( aMethod1, 0 );
    	aDB.addRelationAndTranspose( aClass1, Relation.DECLARES_METHOD, aMethod1 );
    	lSet = aDB.getRange( aClass1, Relation.DECLARES_METHOD );
    	assertTrue( lSet.size() == 1 );
    	assertTrue( lSet.contains( aMethod1 ));
    	lSet = aDB.getRange( aMethod1, Relation.T_DECLARES );
    	assertTrue( lSet.size() == 1 );
    	assertTrue( lSet.contains( aClass1 ));
    }
    
    public void testHasRelations()
    {
    	// Invalid case
    	try
		{
    		aDB.hasRelations( aClass1 );
    		fail( "Expected ElementNotFoundException");
		}
    	catch( ElementNotFoundException pException )
		{}
    	// Empty case
    	aDB.addElement( aClass1, 0 );
    	assertFalse( aDB.hasRelations( aClass1 ));
    	aDB.addElement( aMethod1, 0 );
    	aDB.addRelationAndTranspose( aClass1, Relation.DECLARES_METHOD, aMethod1 );
    	assertTrue( aDB.hasRelations( aClass1 ));
    	assertTrue( aDB.hasRelations( aMethod1 ));
    }
    
    public void testRemoveElement()
    {
    	// Invalid case
    	try
		{
    		aDB.removeElement( aClass1 );
    		fail( "Expected ElementNotFoundException");
		}
    	catch( ElementNotFoundException pException )
		{}
    	aDB.addElement( aClass1, 0 );
    	aDB.removeElement( aClass1 );
    	assertFalse( aDB.contains( aClass1 ));
    	aDB.addElement( aClass1, 0 );
    	aDB.addElement( aMethod1, 0 );
    	aDB.addRelationAndTranspose( aClass1, Relation.DECLARES_METHOD, aMethod1 );
    	aDB.addElement( aClass2, 0 );
    	aDB.addRelationAndTranspose( aClass2, Relation.ACCESSES, aClass1 );
    	aDB.addRelationAndTranspose( aMethod1, Relation.T_DECLARES, aClass2 );
    	aDB.removeElement( aClass1 );
    	assertFalse( aDB.contains( aClass1 ));
    	Set lSet = aDB.getRange( aClass2, Relation.ACCESSES );
    	assertTrue( lSet.size() == 0 );
    	lSet = aDB.getRange( aMethod1, Relation.T_DECLARES );
    	assertTrue( lSet.size() == 1 );
    	assertTrue( lSet.contains( aClass2 ));
    	lSet = aDB.getRange( aClass2, Relation.DECLARES_METHOD );
    	assertTrue( lSet.size() == 1 );
    	assertTrue( lSet.contains( aMethod1 ));
    }
}
