/* JayFX - A Fact Extractor Plug-in for Eclipse
 * Copyright (C) 2006  McGill University (http://www.cs.mcgill.ca/~swevo/jayfx)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * $Revision: 1.9 $
 */

package ca.mcgill.cs.swevo.jayfx.test;

import java.util.Set;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.ui.PlatformUI;

import ca.mcgill.cs.swevo.jayfx.ConversionException;
import ca.mcgill.cs.swevo.jayfx.JayFX;
import ca.mcgill.cs.swevo.jayfx.JayFXException;
import ca.mcgill.cs.swevo.jayfx.RelationNotSupportedException;
import ca.mcgill.cs.swevo.jayfx.model.FlyweightElementFactory;
import ca.mcgill.cs.swevo.jayfx.model.ICategories;
import ca.mcgill.cs.swevo.jayfx.model.IElement;
import ca.mcgill.cs.swevo.jayfx.model.Relation;

public class TestJayFX extends TestCase
{
	private JayFX aDB;
	
	private IElement getElement( String pId, boolean pIsClass )
	{
		if( pIsClass )
			return FlyweightElementFactory.getElement( ICategories.CLASS, pId, null );
		else if( pId.endsWith(")"))
			return FlyweightElementFactory.getElement( ICategories.METHOD, pId, null );
		else
			return FlyweightElementFactory.getElement( ICategories.FIELD, pId, null );
	}
	
	public TestJayFX()
	{
		
	}
	
    protected void setUp() throws Exception 
    {
//    	aDB = new JayFX();
		IProgressMonitor lMonitor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences()[0].getView( true ).getViewSite().getActionBars().getStatusLineManager().getProgressMonitor();
		
//		try
//		{
//			aDB.initialize( ResourcesPlugin.getWorkspace().getRoot().getProject("JayFXBenchmark"), lMonitor, true );
//		}
//		catch( JayFXException pException )
//		{
//			fail( "Cannot load JavaDB " + pException.getMessage() );
//		}
//		aDB.dumpConverter();
    }

    protected void tearDown() throws Exception 
    {
    }
    
    public void testDeclares()
    {
    	Set lRange = aDB.getRange( getElement( "a.b.A", true), Relation.DECLARES );
    	assertEquals( 11, lRange.size() );
    	assertTrue( lRange.contains( getElement( "a.b.A.aTest3", false)));
    	assertTrue( lRange.contains( getElement( "a.b.A$AA", true)));
    	assertTrue( lRange.contains( getElement( "a.b.A.test1(I,Ljava.lang.String;,[Ljava.lang.String;,[[Ljava.lang.String;)", false)));
    	assertTrue( lRange.contains( getElement( "a.b.A.aTest1", false)));
    	assertTrue( lRange.contains( getElement( "a.b.A.<init>()", false)));
    	assertTrue( lRange.contains( getElement( "a.b.A.<init>(Ljava.lang.String;)", false)));
    	assertTrue( lRange.contains( getElement( "a.b.A.<clinit>()", false)));
    	assertTrue( lRange.contains( getElement( "a.b.A.aTest2", false)));
    	assertTrue( lRange.contains( getElement( "a.b.A$AB", true)));
    	assertTrue( lRange.contains( getElement( "a.b.A.aTest0", false)));
    	assertTrue( lRange.contains( getElement( "a.b.A.aTest4", false)));
//    	
//    	lRange = aDB.getRange( getElement( "a.b.A$AA$1", true), Relation.DECLARES );
//    	assertEquals( 2, lRange.size() );
//    	for (Object i : lRange)
//    	{
//    		System.out.println(i.toString());
//    	}
//    	assertTrue( lRange.contains( getElement( "a.b.A$AA$1.doit(La.b.A$AA$1$AAAA;)", false)));
//    	assertTrue( lRange.contains( getElement( "a.b.A$AA$1.doit()", false)));
    	
    }
    
    public void testInnerClassDeclares() {
    	Set lRange = aDB.getRange( getElement( "a.b.A$AA", true), Relation.DECLARES );
    	assertEquals( 2, lRange.size() );
    	assertTrue( lRange.contains( getElement( "a.b.A$AA$AAA", true)));
    	assertTrue( lRange.contains( getElement( "a.b.A$AA.doit(La.b.A$AA$AAA;)", false)));
    }
    
    public void testLocalClassDeclares() 
    {
    	Set lRange = aDB.getRange( getElement( "a.b.A$AA.doit(La.b.A$AA$AAA;)", false), Relation.DECLARES );
    	assertEquals( 3, lRange.size() );
    	
    	assertTrue( lRange.contains( getElement( "a.b.A$AA$1AAAA", true)));
    	assertTrue( lRange.contains( getElement( "a.b.A$AA$1AAAB", true)));
    	assertTrue( lRange.contains( getElement( "a.b.A$AA$1", true)));
    	
    }
    
    public void testInterfaceDeclares() {
    	Set lRange = aDB.getRange( getElement( "a.b.A$AB", true), Relation.DECLARES );
    	assertEquals( 3, lRange.size() );
    	assertTrue( lRange.contains( getElement( "a.b.A$AB.<clinit>()", false)));
    	assertTrue( lRange.contains( getElement( "a.b.A$AB.aTest1", false)));
    	assertTrue( lRange.contains( getElement( "a.b.A$AB.doit()", false)));
    }
    
    public void testSuperClassDeclares() {
    	Set lRange = aDB.getRange( getElement( "java.lang.Object", true ), Relation.DECLARES );
    	assertEquals( 14, lRange.size() );
    	assertTrue( lRange.contains( getElement( "java.lang.Object.registerNatives()", false)));
    	assertTrue( lRange.contains( getElement( "java.lang.Object.wait(J)", false)));
    	assertTrue( lRange.contains( getElement( "java.lang.Object.finalize()", false)));
    	assertTrue( lRange.contains( getElement( "java.lang.Object.toString()", false)));
    	assertTrue( lRange.contains( getElement( "java.lang.Object.hashCode()", false)));
    	assertTrue( lRange.contains( getElement( "java.lang.Object.equals(Ljava.lang.Object;)", false)));
    	assertTrue( lRange.contains( getElement( "java.lang.Object.getClass()", false)));
    	assertTrue( lRange.contains( getElement( "java.lang.Object.notifyAll()", false)));
    	assertTrue( lRange.contains( getElement( "java.lang.Object.clone()", false)));
    	assertTrue( lRange.contains( getElement( "java.lang.Object.notify()", false)));
    	assertTrue( lRange.contains( getElement( "java.lang.Object.wait()", false)));
    	assertTrue( lRange.contains( getElement( "java.lang.Object.<clinit>()", false)));
    	assertTrue( lRange.contains( getElement( "java.lang.Object.<init>()", false)));
    	assertTrue( lRange.contains( getElement( "java.lang.Object.wait(J,I)", false)));
    	
    	lRange = aDB.getRange( getElement( "java.io.FileFilter", true ), Relation.DECLARES );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement( "java.io.FileFilter.accept(Ljava.io.File;)", false)));
    	
    	lRange = aDB.getRange( getElement( "java.awt.BufferCapabilities", true ), Relation.DECLARES );
    	assertEquals( 12, lRange.size() );
    	assertTrue( lRange.contains( getElement( "java.awt.BufferCapabilities$FlipContents", true)));
    	assertTrue( lRange.contains( getElement( "java.awt.BufferCapabilities.<init>(Ljava.awt.ImageCapabilities;,Ljava.awt.ImageCapabilities;,Ljava.awt.BufferCapabilities$FlipContents;)", false)));
    	assertTrue( lRange.contains( getElement( "java.awt.BufferCapabilities.frontCaps", false)));
    	assertTrue( lRange.contains( getElement( "java.awt.BufferCapabilities.flipContents", false)));
    	assertTrue( lRange.contains( getElement( "java.awt.BufferCapabilities.getFrontBufferCapabilities()", false)));
    	assertTrue( lRange.contains( getElement( "java.awt.BufferCapabilities.getFlipContents()", false)));
    	assertTrue( lRange.contains( getElement( "java.awt.BufferCapabilities.isPageFlipping()", false)));
    	assertTrue( lRange.contains( getElement( "java.awt.BufferCapabilities.backCaps", false)));
    	assertTrue( lRange.contains( getElement( "java.awt.BufferCapabilities.clone()", false)));
    	assertTrue( lRange.contains( getElement( "java.awt.BufferCapabilities.getBackBufferCapabilities()", false)));
    	assertTrue( lRange.contains( getElement( "java.awt.BufferCapabilities.isFullScreenRequired()", false)));
    	assertTrue( lRange.contains( getElement( "java.awt.BufferCapabilities.isMultiBufferAvailable()", false)));
    	
    	lRange = aDB.getRange( getElement( "java.awt.BufferCapabilities$FlipContents", true ), Relation.DECLARES );
    	assertEquals( 11, lRange.size() );
    	assertTrue( lRange.contains( getElement( "java.awt.BufferCapabilities$FlipContents.I_UNDEFINED", false)));
    	assertTrue( lRange.contains( getElement( "java.awt.BufferCapabilities$FlipContents.BACKGROUND", false)));
    	assertTrue( lRange.contains( getElement( "java.awt.BufferCapabilities$FlipContents.UNDEFINED", false)));
    	assertTrue( lRange.contains( getElement( "java.awt.BufferCapabilities$FlipContents.PRIOR", false)));
    	assertTrue( lRange.contains( getElement( "java.awt.BufferCapabilities$FlipContents.COPIED", false)));
    	assertTrue( lRange.contains( getElement( "java.awt.BufferCapabilities$FlipContents.I_COPIED", false)));
    	assertTrue( lRange.contains( getElement( "java.awt.BufferCapabilities$FlipContents.I_PRIOR", false)));
    	assertTrue( lRange.contains( getElement( "java.awt.BufferCapabilities$FlipContents.NAMES", false)));
    	assertTrue( lRange.contains( getElement( "java.awt.BufferCapabilities$FlipContents.I_BACKGROUND", false)));
    	assertTrue( lRange.contains( getElement( "java.awt.BufferCapabilities$FlipContents.<init>(I)", false)));
    	assertTrue( lRange.contains( getElement( "java.awt.BufferCapabilities$FlipContents.<clinit>()", false)));
    }
    public void testClassDeclares2() {
    	Set lRange = aDB.getRange( getElement( "a.b.D", true ), Relation.DECLARES );
    	assertEquals( 2, lRange.size() );
    	assertTrue( lRange.contains( getElement( "a.b.D.doit()", false)));
    	assertTrue( lRange.contains( getElement( "a.b.D.equals(Ljava.lang.Object;)", false)));
    }
    
    public void testAccesses()
    {
    	Set lRange = aDB.getRange( getElement( "a.b.A.<clinit>()", false), Relation.ACCESSES );
    	assertEquals( 3, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.A.aTest0", false)));
    	assertTrue( lRange.contains( getElement("a.b.B.aTest1", false)));
    	assertTrue( lRange.contains( getElement("java.lang.System.out", false)));
    	
    	lRange = aDB.getRange( getElement( "a.b.A.<init>()", false), Relation.ACCESSES );
    	assertEquals( 2, lRange.size() );
    	assertTrue( lRange.contains( getElement("java.lang.System.out", false)));
    	assertTrue( lRange.contains( getElement("a.b.A.aTest1", false)));
    	
    	lRange = aDB.getRange( getElement( "a.b.A.test1(I,Ljava.lang.String;,[Ljava.lang.String;,[[Ljava.lang.String;)", false), Relation.ACCESSES );
    	assertEquals( 5, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.A$AB.aTest1", false)));
    	assertTrue( lRange.contains( getElement("a.b.A.aTest1", false)));
    	assertTrue( lRange.contains( getElement("a.b.A.aTest2", false)));
    	assertTrue( lRange.contains( getElement("a.b.A.aTest3", false)));
    	assertTrue( lRange.contains( getElement("a.b.B.aTest1", false)));
    }
    
    public void testTAccesses()
    {
    	Set lRange = aDB.getRange( getElement( "a.b.A.aTest0", false), Relation.T_ACCESSES );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.A.<clinit>()", false)));
    	
    	lRange = aDB.getRange( getElement( "a.b.A.aTest1", false), Relation.T_ACCESSES );
    	assertEquals( 3, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.A.<init>()", false)));
    	assertTrue( lRange.contains( getElement("a.b.A.<init>(Ljava.lang.String;)", false)));
    	assertTrue( lRange.contains( getElement("a.b.A.test1(I,Ljava.lang.String;,[Ljava.lang.String;,[[Ljava.lang.String;)", false)));
    	
    	lRange = aDB.getRange( getElement( "a.b.A.aTest2", false), Relation.T_ACCESSES );
    	assertEquals( 2, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.A.test1(I,Ljava.lang.String;,[Ljava.lang.String;,[[Ljava.lang.String;)", false)));
    	assertTrue( lRange.contains( getElement("a.b.A.<init>(Ljava.lang.String;)", false)));
    	
    	lRange = aDB.getRange( getElement( "a.b.A.aTest3", false), Relation.T_ACCESSES );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.A.test1(I,Ljava.lang.String;,[Ljava.lang.String;,[[Ljava.lang.String;)", false)));
    	
    	lRange = aDB.getRange( getElement( "a.b.A.aTest4", false), Relation.T_ACCESSES );
    	assertEquals( 0, lRange.size() );
    	
    	lRange = aDB.getRange( getElement( "a.b.A$AB.aTest1", false), Relation.T_ACCESSES );
    	assertEquals( 2, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.A.test1(I,Ljava.lang.String;,[Ljava.lang.String;,[[Ljava.lang.String;)", false)));
    	
    	lRange = aDB.getRange( getElement( "a.b.B.aTest1", false), Relation.T_ACCESSES );
    	assertEquals( 3, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.A.test1(I,Ljava.lang.String;,[Ljava.lang.String;,[[Ljava.lang.String;)", false)));
    	assertTrue( lRange.contains( getElement("a.b.A.<clinit>()", false)));
    	assertTrue( lRange.contains( getElement("a.b.B.<init>()", false)));
    }
    
    public void testExtendsClass()
    {
     	Set lRange = aDB.getRange( getElement( "a.b.A", true), Relation.EXTENDS_CLASS );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("java.lang.Object", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.B", true), Relation.EXTENDS_CLASS );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("java.lang.Object", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.A$AA", true), Relation.EXTENDS_CLASS );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("java.lang.Object", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.A$AB", true), Relation.EXTENDS_CLASS );
    	assertEquals( 0, lRange.size() );
    	    	
    	lRange = aDB.getRange( getElement( "a.b.A$AA$1", true), Relation.EXTENDS_CLASS );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("java.lang.Object", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.A$AA$AAA", true), Relation.EXTENDS_CLASS );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("java.lang.Object", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.A$AA$1AAAB", true), Relation.EXTENDS_CLASS );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("java.lang.Object", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.C", true), Relation.EXTENDS_CLASS );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("java.lang.Object", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.D", true), Relation.EXTENDS_CLASS );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("java.lang.Object", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.E", true), Relation.EXTENDS_CLASS );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.D", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.F", true), Relation.EXTENDS_CLASS );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.D", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.G", true), Relation.EXTENDS_CLASS );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.F", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.H", true), Relation.EXTENDS_CLASS );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.F", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.J", true), Relation.EXTENDS_CLASS );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.C", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.I1", true), Relation.EXTENDS_CLASS );
    	assertEquals( 0, lRange.size() );
    	
    	lRange = aDB.getRange( getElement( "java.lang.Object", true), Relation.EXTENDS_CLASS );
    	assertEquals( 0, lRange.size() );
    	
    	lRange = aDB.getRange( getElement( "java.lang.String", true), Relation.EXTENDS_CLASS );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("java.lang.Object", true)));
    }
    
    public void testTExtendsClass()
    {
    	Set lRange = aDB.getRange( getElement( "a.b.A", true), Relation.T_EXTENDS_CLASS );
    	assertEquals( 0, lRange.size() );
    	
    	lRange = aDB.getRange( getElement( "a.b.B", true), Relation.T_EXTENDS_CLASS );
    	assertEquals( 0, lRange.size() );
    	
    	lRange = aDB.getRange( getElement( "a.b.A$AA", true), Relation.T_EXTENDS_CLASS );
    	assertEquals( 0, lRange.size() );
    	
    	lRange = aDB.getRange( getElement( "a.b.A$AB", true), Relation.T_EXTENDS_CLASS );
    	assertEquals( 0, lRange.size() );
    	    	
    	lRange = aDB.getRange( getElement( "a.b.A$AA$1", true), Relation.T_EXTENDS_CLASS );
    	assertEquals( 0, lRange.size() );
    	
    	lRange = aDB.getRange( getElement( "a.b.A$AA$AAA", true), Relation.T_EXTENDS_CLASS );
    	assertEquals( 0, lRange.size() );
    	
    	lRange = aDB.getRange( getElement( "a.b.A$AA$1AAAB", true), Relation.T_EXTENDS_CLASS );
    	assertEquals( 0, lRange.size() );
    	
    	lRange = aDB.getRange( getElement( "a.b.C", true), Relation.T_EXTENDS_CLASS );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.J", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.D", true), Relation.T_EXTENDS_CLASS );
    	assertEquals( 2, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.E", true)));
    	assertTrue( lRange.contains( getElement("a.b.F", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.E", true), Relation.T_EXTENDS_CLASS );
    	assertEquals( 0, lRange.size() );
    	
    	lRange = aDB.getRange( getElement( "a.b.F", true), Relation.T_EXTENDS_CLASS );
    	assertEquals( 2, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.G", true)));
    	assertTrue( lRange.contains( getElement("a.b.H", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.G", true), Relation.T_EXTENDS_CLASS );
    	assertEquals( 0, lRange.size() );
    	
    	lRange = aDB.getRange( getElement( "a.b.H", true), Relation.T_EXTENDS_CLASS );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.K", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.J", true), Relation.T_EXTENDS_CLASS );
    	assertEquals( 0, lRange.size() );
    	
    	lRange = aDB.getRange( getElement( "a.b.I1", true), Relation.T_EXTENDS_CLASS );
    	assertEquals( 0, lRange.size() );
    }
    
    public void testExtendsInterfaces()
    {
    	Set lRange = aDB.getRange( getElement( "a.b.A", true), Relation.EXTENDS_INTERFACES );
    	assertEquals( 0, lRange.size() );
    	
    	lRange = aDB.getRange( getElement( "a.b.I1", true), Relation.EXTENDS_INTERFACES );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.I2", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.I2", true), Relation.EXTENDS_INTERFACES );
    	assertEquals( 0, lRange.size() );
    	
    	lRange = aDB.getRange( getElement( "a.b.I3", true), Relation.EXTENDS_INTERFACES );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.I2", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.I4", true), Relation.EXTENDS_INTERFACES );
    	assertEquals( 0, lRange.size() );

    	lRange = aDB.getRange( getElement( "a.b.I5", true), Relation.EXTENDS_INTERFACES );
    	assertEquals( 2, lRange.size() );
    	assertTrue( lRange.contains( getElement("java.lang.Runnable", true)));
    	assertTrue( lRange.contains( getElement("java.io.Serializable", true)));
    	
    }
    
    public void testNonProjectExtendsInterfaces()
    {
    	Set lRange = aDB.getRange( getElement( "javax.imageio.stream.ImageInputStream", true), Relation.EXTENDS_INTERFACES );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("java.io.DataInput", true)));
    	
    	lRange = aDB.getRange( getElement( "java.io.Externalizable", true), Relation.EXTENDS_INTERFACES );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("java.io.Serializable", true)));
    	
    	lRange = aDB.getRange( getElement( "java.util.SortedSet", true), Relation.EXTENDS_INTERFACES );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("java.util.Set<TE;>", true)));
    }
    
    public void testTExtendInterfaces()
    {
    	Set lRange = aDB.getRange( getElement( "a.b.A", true), Relation.T_EXTENDS_INTERFACES );
    	assertEquals( 0, lRange.size() );
    	
    	lRange = aDB.getRange( getElement( "a.b.I1", true), Relation.T_EXTENDS_INTERFACES );
    	assertEquals( 0, lRange.size() );
    	
    	lRange = aDB.getRange( getElement( "a.b.I2", true), Relation.T_EXTENDS_INTERFACES );
    	assertEquals( 3, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.I1", true)));
    	assertTrue( lRange.contains( getElement("a.b.I3", true)));
    	assertTrue( lRange.contains( getElement("a.b.I6", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.I3", true), Relation.T_EXTENDS_INTERFACES );
    	assertEquals( 0, lRange.size() );
    	    	
    	lRange = aDB.getRange( getElement( "a.b.I4", true), Relation.T_EXTENDS_INTERFACES );
    	assertEquals( 0, lRange.size() );

    	lRange = aDB.getRange( getElement( "a.b.I5", true), Relation.T_EXTENDS_INTERFACES );
    	assertEquals( 0, lRange.size() );
    	
    }
    public void testNonProjectTExtendInterfaces(){
    	Set lRange = aDB.getRange( getElement( "java.lang.Runnable", true), Relation.T_EXTENDS_INTERFACES );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.I5", true)));
    	
    	lRange = aDB.getRange( getElement( "java.io.Serializable", true), Relation.T_EXTENDS_INTERFACES );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.I5", true)));
    }
    
    public void testImplementsInterface()
    {
    	Set lRange = aDB.getRange( getElement( "a.b.A", true), Relation.IMPLEMENTS_INTERFACE );
    	assertEquals( 0, lRange.size() );
    	
    	lRange = aDB.getRange( getElement( "a.b.A$AA$1", true), Relation.IMPLEMENTS_INTERFACE );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.A$AB", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.C", true), Relation.IMPLEMENTS_INTERFACE );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.I1", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.C", true), Relation.IMPLEMENTS_INTERFACE );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.I1", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.D", true), Relation.IMPLEMENTS_INTERFACE );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.I2", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.E", true), Relation.IMPLEMENTS_INTERFACE );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.I3", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.F", true), Relation.IMPLEMENTS_INTERFACE );
    	assertEquals( 0, lRange.size() );
    	
    	lRange = aDB.getRange( getElement( "a.b.G", true), Relation.IMPLEMENTS_INTERFACE );
    	assertEquals( 0, lRange.size() );
    	
    	lRange = aDB.getRange( getElement( "a.b.H", true), Relation.IMPLEMENTS_INTERFACE );
    	assertEquals( 2, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.I4", true)));
    	assertTrue( lRange.contains( getElement("a.b.I5", true)));
    	
    	
    	lRange = aDB.getRange( getElement( "a.b.I1", true), Relation.IMPLEMENTS_INTERFACE );
    	assertEquals( 0, lRange.size() );
    	
    	lRange = aDB.getRange( getElement( "a.b.I2", true), Relation.IMPLEMENTS_INTERFACE );
    	assertEquals( 0, lRange.size() );
    	
    	lRange = aDB.getRange( getElement( "a.b.I3", true), Relation.IMPLEMENTS_INTERFACE );
    	assertEquals( 0, lRange.size() );
    	
    	lRange = aDB.getRange( getElement( "a.b.I4", true), Relation.IMPLEMENTS_INTERFACE );
    	assertEquals( 0, lRange.size() );
    	
    	lRange = aDB.getRange( getElement( "a.b.I5", true), Relation.IMPLEMENTS_INTERFACE );
    	assertEquals( 0, lRange.size() );
    	
    	lRange = aDB.getRange( getElement( "a.b.J", true), Relation.IMPLEMENTS_INTERFACE );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.I6", true)));
    	
    	lRange = aDB.getRange( getElement( "java.lang.String", true), Relation.IMPLEMENTS_INTERFACE );
    	assertEquals( 3, lRange.size() );
    	assertTrue( lRange.contains( getElement("java.io.Serializable", true)));
    	assertTrue( lRange.contains( getElement("java.lang.Comparable<Ljava.lang.String;>", true)));
    	assertTrue( lRange.contains( getElement("java.lang.CharSequence", true)));
    }
    
    public void testTImplementsInterface()
    {
    	Set lRange = aDB.getRange( getElement( "a.b.A", true), Relation.T_IMPLEMENTS_INTERFACE );
    	assertEquals( 0, lRange.size() );
    	
    	lRange = aDB.getRange( getElement( "a.b.A$AB", true), Relation.T_IMPLEMENTS_INTERFACE );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.A$AA$1", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.I", true), Relation.T_IMPLEMENTS_INTERFACE );
    	assertEquals( 0, lRange.size() );
    	
    	lRange = aDB.getRange( getElement( "a.b.I1", true), Relation.T_IMPLEMENTS_INTERFACE );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.C", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.I2", true), Relation.T_IMPLEMENTS_INTERFACE );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.D", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.I3", true), Relation.T_IMPLEMENTS_INTERFACE );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.E", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.I4", true), Relation.T_IMPLEMENTS_INTERFACE );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.H", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.I5", true), Relation.T_IMPLEMENTS_INTERFACE );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.H", true)));
    }
    
    public void testTransExtends()
    {
        Set lRange = aDB.getRange( getElement( "a.b.A$AB", true), Relation.TRANS_EXTENDS );
    	assertEquals( 0, lRange.size() ); 
    	
    	lRange = aDB.getRange( getElement( "a.b.I1", true), Relation.TRANS_EXTENDS );
    	assertEquals( 0, lRange.size() );   
    	
    	lRange = aDB.getRange( getElement( "a.b.A", true), Relation.TRANS_EXTENDS );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("java.lang.Object", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.B", true), Relation.TRANS_EXTENDS );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("java.lang.Object", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.C", true), Relation.TRANS_EXTENDS );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("java.lang.Object", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.D", true), Relation.TRANS_EXTENDS );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("java.lang.Object", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.E", true), Relation.TRANS_EXTENDS );
    	assertEquals( 2, lRange.size() );
    	assertTrue( lRange.contains( getElement("java.lang.Object", true)));
    	assertTrue( lRange.contains( getElement("a.b.D", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.F", true), Relation.TRANS_EXTENDS );
    	assertEquals( 2, lRange.size() );
    	assertTrue( lRange.contains( getElement("java.lang.Object", true)));
    	assertTrue( lRange.contains( getElement("a.b.D", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.G", true), Relation.TRANS_EXTENDS );
    	assertEquals( 3, lRange.size() );
    	assertTrue( lRange.contains( getElement("java.lang.Object", true)));
    	assertTrue( lRange.contains( getElement("a.b.D", true)));
    	assertTrue( lRange.contains( getElement("a.b.F", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.H", true), Relation.TRANS_EXTENDS );
    	assertEquals( 3, lRange.size() );
    	assertTrue( lRange.contains( getElement("java.lang.Object", true)));
    	assertTrue( lRange.contains( getElement("a.b.D", true)));
    	assertTrue( lRange.contains( getElement("a.b.F", true)));
    	
    	
    	lRange = aDB.getRange( getElement( "a.b.J", true), Relation.TRANS_EXTENDS );
    	assertEquals( 2, lRange.size() );
    	assertTrue( lRange.contains( getElement("java.lang.Object", true)));
    	assertTrue( lRange.contains( getElement("a.b.C", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.K", true), Relation.TRANS_EXTENDS );
    	assertEquals( 4, lRange.size() );
    	assertTrue( lRange.contains( getElement("java.lang.Object", true)));
    	assertTrue( lRange.contains( getElement("a.b.D", true)));
    	assertTrue( lRange.contains( getElement("a.b.F", true)));
    	assertTrue( lRange.contains( getElement("a.b.H", true)));
    }
    
    public void testTTransExtends()
    {
        Set lRange = aDB.getRange( getElement( "a.b.A$AB", true), Relation.T_TRANS_EXTENDS );
    	assertEquals( 0, lRange.size() ); 
    	
    	lRange = aDB.getRange( getElement( "a.b.A$AA$1", true), Relation.T_TRANS_EXTENDS );
    	assertEquals( 0, lRange.size() ); 
    	
    	lRange = aDB.getRange( getElement( "a.b.I1", true), Relation.T_TRANS_EXTENDS );
    	assertEquals( 0, lRange.size() ); 
    	
    	lRange = aDB.getRange( getElement( "a.b.C", true), Relation.T_TRANS_EXTENDS );
    	assertEquals( 1, lRange.size() ); 
    	assertTrue( lRange.contains( getElement("a.b.J", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.D", true), Relation.T_TRANS_EXTENDS );
    	assertEquals( 5, lRange.size() ); 
    	assertTrue( lRange.contains( getElement("a.b.E", true)));
    	assertTrue( lRange.contains( getElement("a.b.F", true)));
    	assertTrue( lRange.contains( getElement("a.b.G", true)));
    	assertTrue( lRange.contains( getElement("a.b.H", true)));
    	assertTrue( lRange.contains( getElement("a.b.K", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.E", true), Relation.T_TRANS_EXTENDS );
    	assertEquals( 0, lRange.size() ); 
    	
    	lRange = aDB.getRange( getElement( "a.b.F", true), Relation.T_TRANS_EXTENDS );
    	assertEquals( 3, lRange.size() ); 
    	assertTrue( lRange.contains( getElement("a.b.G", true)));
    	assertTrue( lRange.contains( getElement("a.b.H", true)));
    	assertTrue( lRange.contains( getElement("a.b.K", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.G", true), Relation.T_TRANS_EXTENDS );
    	assertEquals( 0, lRange.size() ); 
    	
    	lRange = aDB.getRange( getElement( "a.b.H", true), Relation.T_TRANS_EXTENDS );
    	assertEquals( 1, lRange.size() ); 
    	assertTrue( lRange.contains( getElement("a.b.K", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.I", true), Relation.T_TRANS_EXTENDS );
    	assertEquals( 0, lRange.size() ); 
    	
    	lRange = aDB.getRange( getElement( "a.b.J", true), Relation.T_TRANS_EXTENDS );
    	assertEquals( 0, lRange.size() ); 
    }
    
    public void testConverter() throws ConversionException
    {
    	// Valid two-way conversions
    	IElement[] lInitial = new IElement[18];
    	lInitial[0] = getElement( "a.b.A", true);
    	lInitial[1] = getElement( "a.b.A.aTest0", false);
    	lInitial[2] = getElement( "a.b.A.aTest1", false);
    	lInitial[3] = getElement( "a.b.A.aTest2", false);
    	lInitial[4] = getElement( "a.b.A.aTest3", false);
    	lInitial[5] = getElement( "a.b.A.aTest4", false);
    	lInitial[6] = getElement( "a.b.A.test1(I,Ljava.lang.String;,[Ljava.lang.String;,[[Ljava.lang.String;)", false);
    	lInitial[7] = getElement( "a.b.A.<init>(Ljava.lang.String;)", false);
    	lInitial[8] = getElement( "a.b.A$AA", true);
    	lInitial[9] = getElement( "a.b.A$AA$AAA", true);
    	lInitial[10] = getElement( "a.b.A$AA.doit(La.b.A$AA$AAA;)", false);
    	lInitial[11] = getElement( "a.b.A$AB", true);
    	lInitial[12] = getElement( "a.b.A$AB.aTest1", false);
    	lInitial[13] = getElement( "a.b.B", true);
    	lInitial[14] = getElement( "a.b.B.aTest2", false);
    	lInitial[15] = getElement( "java.lang.Runnable", true);
    	lInitial[16] = getElement( "java.io.PrintStream.println(Ljava.lang.String;)", false);
    	lInitial[17] = getElement( "a.b.I2", true);
    	
    	for( int i = 0; i < lInitial.length; i++ )
    	{
    		IJavaElement lJavaElement = aDB.convertToJavaElement( lInitial[i] );
    		IElement lFinal = aDB.convertToElement( lJavaElement );
    		assertTrue( lInitial[i] == lFinal );
    	}
    	
    	// Invalid two-way conversions
    	// To make sure these raise the proper exception and do not case a crash
    	lInitial = new IElement[6];
    	lInitial[0] = getElement( "a.b.A$AA$1.doit(La.b.A$AA$1AAAA;)", false);
    	lInitial[1] = getElement( "a.b.A$AB.<clinit>()", false);
    	lInitial[2] = getElement( "a.b.A$1$AAAB", false);
    	lInitial[3] = getElement( "a.b.A$1", false);
    	lInitial[4] = getElement( "a.b.B.<init>()", false);
    	lInitial[5] = getElement( "a.b.A.<init>()", false);
    	
    	for( int i = 0; i < lInitial.length; i++ )
    	{
    		try
			{
    			IJavaElement lJavaElement = aDB.convertToJavaElement( lInitial[i] );
    			fail( "Expected ConversionException");
			}
    		catch( ConversionException pException )
			{}
    	}
    	
    }
    
    public void testOverrides()
    {
        Set lRange = aDB.getRange( getElement( "a.b.C.doit()", false), Relation.OVERRIDES );
    	assertEquals( 0, lRange.size() ); 
    	
    	lRange = aDB.getRange( getElement( "a.b.D.doit()", false), Relation.OVERRIDES );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.I2.doit()", false)));
    	
    	lRange = aDB.getRange( getElement( "a.b.D.equals(Ljava.lang.Object;)", false), Relation.OVERRIDES );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("java.lang.Object.equals(Ljava.lang.Object;)", false)));
    	
    	lRange = aDB.getRange( getElement( "a.b.E.doit()", false), Relation.OVERRIDES );
    	assertEquals( 2, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.D.doit()", false)));
    	assertTrue( lRange.contains( getElement("a.b.I2.doit()", false)));
    	
    	lRange = aDB.getRange( getElement( "a.b.F.doit()", false), Relation.OVERRIDES );
    	assertEquals( 2, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.D.doit()", false)));
    	assertTrue( lRange.contains( getElement("a.b.I2.doit()", false)));
    	
    	lRange = aDB.getRange( getElement( "a.b.G.doit()", false), Relation.OVERRIDES );
    	assertEquals( 3, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.D.doit()", false)));
    	assertTrue( lRange.contains( getElement("a.b.I2.doit()", false)));
    	assertTrue( lRange.contains( getElement("a.b.F.doit()", false)));
    	
    	lRange = aDB.getRange( getElement( "a.b.H.run()", false), Relation.OVERRIDES );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("java.lang.Runnable.run()", false)));
    	
    	lRange = aDB.getRange( getElement( "a.b.H.toString()", false), Relation.OVERRIDES );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("java.lang.Object.toString()", false)));
    	
    	lRange = aDB.getRange( getElement( "a.b.I2.doit()", false), Relation.OVERRIDES );
    	assertEquals( 0, lRange.size() );
    	
    	lRange = aDB.getRange( getElement( "a.b.J.doit()", false), Relation.OVERRIDES );
    	assertEquals( 4, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.C.doit()", false)));
    	assertTrue( lRange.contains( getElement("a.b.I1.doit()", false)));
    	assertTrue( lRange.contains( getElement("a.b.I2.doit()", false)));
    	assertTrue( lRange.contains( getElement("a.b.I6.doit()", false)));
    	
    	lRange = aDB.getRange( getElement( "a.b.K.doit()", false), Relation.OVERRIDES );
    	assertEquals( 3, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.F.doit()", false)));
    	assertTrue( lRange.contains( getElement("a.b.D.doit()", false)));
    	assertTrue( lRange.contains( getElement("a.b.I2.doit()", false)));
    }
    
    public void testTOverrides()
    {
        Set lRange = aDB.getRange( getElement( "a.b.A$AA.doit(La.b.A$AA$AAA;)", false), Relation.T_OVERRIDES );
    	assertEquals( 0, lRange.size() ); 
    	
    	lRange = aDB.getRange( getElement( "a.b.C.doit()", false), Relation.T_OVERRIDES );
    	assertEquals( 1, lRange.size() ); 
    	assertTrue( lRange.contains( getElement("a.b.J.doit()", false)));
    	
    	lRange = aDB.getRange( getElement( "a.b.D.doit()", false), Relation.T_OVERRIDES );
    	assertEquals( 4, lRange.size() ); 
    	assertTrue( lRange.contains( getElement("a.b.E.doit()", false)));
    	assertTrue( lRange.contains( getElement("a.b.F.doit()", false)));
    	assertTrue( lRange.contains( getElement("a.b.G.doit()", false)));
    	assertTrue( lRange.contains( getElement("a.b.K.doit()", false)));
    	
    	lRange = aDB.getRange( getElement( "java.lang.Object.equals(Ljava.lang.Object;)", false), Relation.T_OVERRIDES );
//    	for (Object i : lRange)
//        	{
//        		System.out.println(i.toString());
//        	}
    	assertEquals( 2, lRange.size() );  // TODO: it overrides itself as well??
    	assertTrue( lRange.contains( getElement("a.b.D.equals(Ljava.lang.Object;)", false)));
    	
    	lRange = aDB.getRange( getElement( "a.b.E.doit()", false), Relation.T_OVERRIDES );
    	assertEquals( 0, lRange.size() ); 
    	
    	lRange = aDB.getRange( getElement( "a.b.F.doit()", false), Relation.T_OVERRIDES );
    	assertEquals( 2, lRange.size() ); 
    	assertTrue( lRange.contains( getElement("a.b.K.doit()", false)));
    	assertTrue( lRange.contains( getElement("a.b.G.doit()", false)));
    	
    	lRange = aDB.getRange( getElement( "a.b.G.doit()", false), Relation.T_OVERRIDES );
    	assertEquals( 0, lRange.size() ); 
   	
    	lRange = aDB.getRange( getElement( "java.lang.Object.toString()", false), Relation.T_OVERRIDES );
    	assertEquals( 2, lRange.size() ); 
    	assertTrue( lRange.contains( getElement("a.b.K.toString()", false)));
    	assertTrue( lRange.contains( getElement("a.b.H.toString()", false)));
    	
    	lRange = aDB.getRange( getElement( "a.b.I1.doit()", false), Relation.T_OVERRIDES );
    	assertEquals( 1, lRange.size() ); 
    	assertTrue( lRange.contains( getElement("a.b.J.doit()", false)));

    	lRange = aDB.getRange( getElement( "a.b.I2.doit()", false), Relation.T_OVERRIDES );
    	assertEquals( 6, lRange.size() ); 
    	assertTrue( lRange.contains( getElement("a.b.D.doit()", false)));
    	assertTrue( lRange.contains( getElement("a.b.E.doit()", false)));
    	assertTrue( lRange.contains( getElement("a.b.F.doit()", false)));
    	assertTrue( lRange.contains( getElement("a.b.G.doit()", false)));
    	assertTrue( lRange.contains( getElement("a.b.J.doit()", false)));
    	assertTrue( lRange.contains( getElement("a.b.K.doit()", false)));
    }
    
    public void testTransitivelyImplements()
    {
    	Set lRange = aDB.getRange( getElement( "java.lang.Object", true), Relation.TRANS_IMPLEMENTS );
    	assertEquals( 0, lRange.size() ); 
    	
    	lRange = aDB.getRange( getElement( "a.b.A", true), Relation.TRANS_IMPLEMENTS );
    	assertEquals( 0, lRange.size() ); 
    	
    	lRange = aDB.getRange( getElement( "a.b.B", true), Relation.TRANS_IMPLEMENTS );
    	assertEquals( 0, lRange.size() ); 
    	
    	lRange = aDB.getRange( getElement( "a.b.A$AA", true), Relation.TRANS_IMPLEMENTS );
    	assertEquals( 0, lRange.size() ); 
    	
    	lRange = aDB.getRange( getElement( "a.b.A$AA$AAA", true), Relation.TRANS_IMPLEMENTS );
    	assertEquals( 0, lRange.size() ); 
    	
    	lRange = aDB.getRange( getElement( "a.b.A$AA$1", true), Relation.TRANS_IMPLEMENTS );
    	assertEquals( 1, lRange.size() ); 
    	assertTrue( lRange.contains( getElement("a.b.A$AB", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.C", true), Relation.TRANS_IMPLEMENTS );
    	assertEquals( 2, lRange.size() ); 
    	assertTrue( lRange.contains( getElement("a.b.I1", true)));
    	assertTrue( lRange.contains( getElement("a.b.I2", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.D", true), Relation.TRANS_IMPLEMENTS );
    	assertEquals( 1, lRange.size() ); 
    	assertTrue( lRange.contains( getElement("a.b.I2", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.E", true), Relation.TRANS_IMPLEMENTS );
    	assertEquals( 2, lRange.size() ); 
    	assertTrue( lRange.contains( getElement("a.b.I2", true)));
    	assertTrue( lRange.contains( getElement("a.b.I3", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.F", true), Relation.TRANS_IMPLEMENTS );
    	assertEquals( 1, lRange.size() ); 
    	assertTrue( lRange.contains( getElement("a.b.I2", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.G", true), Relation.TRANS_IMPLEMENTS );
    	assertEquals( 1, lRange.size() ); 
    	assertTrue( lRange.contains( getElement("a.b.I2", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.H", true), Relation.TRANS_IMPLEMENTS );
    	assertEquals( 5, lRange.size() ); 
    	assertTrue( lRange.contains( getElement("a.b.I2", true)));
    	assertTrue( lRange.contains( getElement("a.b.I4", true)));
    	assertTrue( lRange.contains( getElement("a.b.I5", true)));
    	assertTrue( lRange.contains( getElement("java.lang.Runnable", true)));
    	assertTrue( lRange.contains( getElement("java.io.Serializable", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.K", true), Relation.TRANS_IMPLEMENTS );
    	assertEquals( 5, lRange.size() ); 
    	assertTrue( lRange.contains( getElement("a.b.I2", true)));
    	assertTrue( lRange.contains( getElement("a.b.I4", true)));
    	assertTrue( lRange.contains( getElement("a.b.I5", true)));
    	assertTrue( lRange.contains( getElement("java.lang.Runnable", true)));
    	assertTrue( lRange.contains( getElement("java.io.Serializable", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.J", true), Relation.TRANS_IMPLEMENTS );
    	assertEquals( 3, lRange.size() ); 
    	assertTrue( lRange.contains( getElement("a.b.I1", true)));
    	assertTrue( lRange.contains( getElement("a.b.I2", true)));
    	assertTrue( lRange.contains( getElement("a.b.I6", true)));
    }
    
    public void testTTransitivelyImplements()
    {
    	Set lRange = aDB.getRange( getElement( "java.lang.Object", true), Relation.T_TRANS_IMPLEMENTS );
    	assertEquals( 0, lRange.size() ); 
    	
    	lRange = aDB.getRange( getElement( "a.b.A", true), Relation.T_TRANS_IMPLEMENTS );
    	assertEquals( 0, lRange.size() ); 
    	
    	lRange = aDB.getRange( getElement( "a.b.A$AB", true), Relation.T_TRANS_IMPLEMENTS );
    	assertEquals( 1, lRange.size() ); 
    	assertTrue( lRange.contains( getElement("a.b.A$AA$1", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.I1", true), Relation.T_TRANS_IMPLEMENTS );
    	assertEquals( 2, lRange.size() ); 
    	assertTrue( lRange.contains( getElement("a.b.C", true)));
    	assertTrue( lRange.contains( getElement("a.b.J", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.I2", true), Relation.T_TRANS_IMPLEMENTS );
    	assertEquals( 8, lRange.size() ); 
    	assertTrue( lRange.contains( getElement("a.b.C", true)));
    	assertTrue( lRange.contains( getElement("a.b.J", true)));
    	assertTrue( lRange.contains( getElement("a.b.E", true)));
    	assertTrue( lRange.contains( getElement("a.b.F", true)));
    	assertTrue( lRange.contains( getElement("a.b.G", true)));
    	assertTrue( lRange.contains( getElement("a.b.H", true)));
    	assertTrue( lRange.contains( getElement("a.b.D", true)));
    	assertTrue( lRange.contains( getElement("a.b.K", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.I3", true), Relation.T_TRANS_IMPLEMENTS );
    	assertEquals( 1, lRange.size() ); 
    	assertTrue( lRange.contains( getElement("a.b.E", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.I4", true), Relation.T_TRANS_IMPLEMENTS );
    	assertEquals( 2, lRange.size() ); 
    	assertTrue( lRange.contains( getElement("a.b.H", true)));
    	assertTrue( lRange.contains( getElement("a.b.K", true)));
    	
    	lRange = aDB.getRange( getElement( "a.b.I5", true), Relation.T_TRANS_IMPLEMENTS );
    	assertEquals( 2, lRange.size() ); 
    	assertTrue( lRange.contains( getElement("a.b.H", true)));
    	assertTrue( lRange.contains( getElement("a.b.K", true)));
    	
    	lRange = aDB.getRange( getElement( "java.lang.Runnable", true), Relation.T_TRANS_IMPLEMENTS );
    	assertEquals( 2, lRange.size() ); 
    	assertTrue( lRange.contains( getElement("a.b.H", true)));
    	assertTrue( lRange.contains( getElement("a.b.K", true)));
    	
    	lRange = aDB.getRange( getElement( "java.io.Serializable", true), Relation.T_TRANS_IMPLEMENTS );
    	assertEquals( 2, lRange.size() ); 
    	assertTrue( lRange.contains( getElement("a.b.H", true)));
    	assertTrue( lRange.contains( getElement("a.b.K", true)));
    }
    
    public void testIsProjectElement()
    {
    	assertTrue( aDB.isProjectElement( getElement("a.b.c.C2.<init>()", false)));
    	assertTrue( aDB.isProjectElement( getElement("a.b.E", true)));
    	assertTrue( aDB.isProjectElement( getElement("a.b.c.C1.doit(Ljava.lang.String;)", false)));
    	assertTrue( aDB.isProjectElement( getElement("a.b.A.aTest2", false)));
    	assertTrue( aDB.isProjectElement( getElement("a.b.A$1.doit(La.b.A$1$AAAA;)", false)));
    	assertTrue( aDB.isProjectElement( getElement("a.b.D.doit()", false)));
    	assertTrue( aDB.isProjectElement( getElement("a.b.F", true)));
    	assertTrue( aDB.isProjectElement( getElement("a.b.I2", true)));
    	assertTrue( aDB.isProjectElement( getElement("a.b.c.C1.main([Ljava.lang.String;)", false)));
    	assertTrue( aDB.isProjectElement( getElement("a.b.D.equals(Ljava.lang.Object;)", false)));
    	assertTrue( aDB.isProjectElement( getElement("a.b.A.test1(I,Ljava.lang.String;,[Ljava.lang.String;,[[Ljava.lang.String;)", false)));
    	assertTrue( aDB.isProjectElement( getElement("a.b.A$AB.doit()", false)));
    	assertTrue( aDB.isProjectElement( getElement("a.b.A$1$AAAA", true)));
    	
    	assertFalse( aDB.isProjectElement( getElement("java.lang.Object", true)));
    	assertFalse( aDB.isProjectElement( getElement("java.lang.Runnable", true)));
    	assertFalse( aDB.isProjectElement( getElement("java.io.PrintStream.println(Ljava.lang.String;)", false)));
    	assertFalse( aDB.isProjectElement( getElement("java.io.PrintStream.println(I)", false)));
    }
    
    public void testCalls()
    {
    	Set lRange = aDB.getRange( getElement( "a.b.c.C1.main([Ljava.lang.String;)", false), Relation.CALLS );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.A.<init>(Ljava.lang.String;)", false)));
    	
    	// Calls to default super constructors are not included in the database
    	lRange = aDB.getRange( getElement( "a.b.A.<init>()", false), Relation.CALLS );
    	assertTrue( lRange.contains( getElement("java.io.PrintStream.println(Ljava.lang.String;)", false)));
    	assertEquals( 1, lRange.size() );
    	    	
    	lRange = aDB.getRange( getElement( "a.b.c.C2.<init>()", false), Relation.CALLS );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.c.C1.<init>(Ljava.lang.String;)", false)));
    	
    	lRange = aDB.getRange( getElement( "a.b.c.C2.<init>(Ljava.lang.String;)", false), Relation.CALLS );
    	assertEquals( 2, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.c.C1.<init>(Ljava.lang.String;)", false)));
    	assertTrue( lRange.contains( getElement("java.io.PrintStream.println(Ljava.lang.String;)", false)));
    	
    	lRange = aDB.getRange( getElement( "a.b.c.C2.<init>(Ljava.lang.String;,I)", false), Relation.CALLS );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.c.C2.<init>(Ljava.lang.String;)", false)));
    	
    	lRange = aDB.getRange( getElement( "a.b.c.C2.doit(Ljava.lang.String;)", false), Relation.CALLS );
    	assertEquals( 2, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.c.C1.doit(Ljava.lang.String;)", false)));
    	assertTrue( lRange.contains( getElement("java.io.PrintStream.println(Ljava.lang.String;)", false)));

    	lRange = aDB.getRange( getElement( "a.b.d.D1.doit()", false), Relation.CALLS );
    	assertEquals( 3, lRange.size() );
        assertTrue( lRange.contains( getElement("a.b.H.<init>()", false)));
        assertTrue( lRange.contains( getElement("a.b.H.toString()", false)));
        assertTrue( lRange.contains( getElement("a.b.K.toString()", false)));
        
        lRange = aDB.getRange( getElement( "a.b.c.C3.doit()", false), Relation.CALLS );
        assertEquals( 8, lRange.size() );
        assertTrue( lRange.contains( getElement("a.b.I2.doit()", false)));
        assertTrue( lRange.contains( getElement("a.b.J.doit()", false)));
        assertTrue( lRange.contains( getElement("a.b.D.doit()", false)));
        assertTrue( lRange.contains( getElement("a.b.E.doit()", false)));
        assertTrue( lRange.contains( getElement("a.b.F.doit()", false)));
        assertTrue( lRange.contains( getElement("a.b.G.doit()", false)));
        assertTrue( lRange.contains( getElement("a.b.K.doit()", false)));
        assertTrue( lRange.contains( getElement("a.b.D.<init>()", false)));
    }
    
    public void testGetRangeInProject()
    {
    	Set lRange = aDB.getRangeInProject( getElement( "a.b.c.C1.main([Ljava.lang.String;)", false), Relation.CALLS );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.A.<init>(Ljava.lang.String;)", false)));
    	
    	lRange = aDB.getRangeInProject( getElement( "a.b.A.<init>()", false), Relation.CALLS );
    	assertEquals( 0, lRange.size() );
    	    	
    	lRange = aDB.getRangeInProject( getElement( "a.b.c.C2.<init>()", false), Relation.CALLS );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.c.C1.<init>(Ljava.lang.String;)", false)));
    	
    	lRange = aDB.getRangeInProject( getElement( "a.b.c.C2.<init>(Ljava.lang.String;)", false), Relation.CALLS );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.c.C1.<init>(Ljava.lang.String;)", false)));
    	    	
    	lRange = aDB.getRangeInProject( getElement( "a.b.c.C2.<init>(Ljava.lang.String;,I)", false), Relation.CALLS );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.c.C2.<init>(Ljava.lang.String;)", false)));
    	
    	lRange = aDB.getRangeInProject( getElement( "a.b.c.C2.doit(Ljava.lang.String;)", false), Relation.CALLS );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.c.C1.doit(Ljava.lang.String;)", false)));
    	
    	lRange = aDB.getRangeInProject( getElement( "a.b.d.D1.doit()", false), Relation.CALLS );
        assertEquals( 3, lRange.size() );
        assertTrue( lRange.contains( getElement("a.b.H.<init>()", false)));
        assertTrue( lRange.contains( getElement("a.b.H.toString()", false)));
        assertTrue( lRange.contains( getElement("a.b.K.toString()", false)));
        
        lRange = aDB.getRangeInProject( getElement( "a.b.c.C3.doit()", false), Relation.CALLS );
        assertEquals( 8, lRange.size() );
        assertTrue( lRange.contains( getElement("a.b.I2.doit()", false)));
        assertTrue( lRange.contains( getElement("a.b.J.doit()", false)));
        assertTrue( lRange.contains( getElement("a.b.D.doit()", false)));
        assertTrue( lRange.contains( getElement("a.b.E.doit()", false)));
        assertTrue( lRange.contains( getElement("a.b.F.doit()", false)));
        assertTrue( lRange.contains( getElement("a.b.G.doit()", false)));
        assertTrue( lRange.contains( getElement("a.b.K.doit()", false)));
        assertTrue( lRange.contains( getElement("a.b.D.<init>()", false)));
    }
    
    public void testTCalls()
    {
    	Set lRange = aDB.getRange( getElement( "a.b.B.<init>()", false), Relation.T_CALLS );
    	assertEquals( 2, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.A.test1(I,Ljava.lang.String;,[Ljava.lang.String;,[[Ljava.lang.String;)", false)));
    	assertTrue( lRange.contains( getElement("a.b.A.<clinit>()", false)));
    	
    	lRange = aDB.getRange( getElement( "a.b.I2.doit()", false), Relation.T_CALLS );
    	assertEquals( 1, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.c.C3.doit()", false)));
    	
    	lRange = aDB.getRange( getElement( "a.b.K.doit()", false), Relation.T_CALLS );
    	assertEquals( 2, lRange.size() );
    	assertTrue( lRange.contains( getElement("a.b.c.C3.doit()", false)));
    	assertTrue( lRange.contains( getElement("a.b.d.D1.m1()", false)));
    }
    
    public void testEnumDeclares()
    {
    	Set lRange = aDB.getRange( getElement( "ca.cs.mcgill.swevo.jayfxbenchmark.enumtest.Planet", true), Relation.DECLARES );
    	for (Object i : lRange)
    	{
    		System.out.println(i.toString());
    	}
    	assertEquals( 21, lRange.size() ); // isn't it 22? 
    	assertTrue( lRange.contains( getElement( "ca.cs.mcgill.swevo.jayfxbenchmark.enumtest.Planet.MERCURY" , false)));
    	assertTrue( lRange.contains( getElement( "ca.cs.mcgill.swevo.jayfxbenchmark.enumtest.Planet.VENUS" , false)));
    	assertTrue( lRange.contains( getElement( "ca.cs.mcgill.swevo.jayfxbenchmark.enumtest.Planet.PLUTO" , false)));
    	assertTrue( lRange.contains( getElement( "ca.cs.mcgill.swevo.jayfxbenchmark.enumtest.Planet.mass" , false)));
    	assertTrue( lRange.contains( getElement( "ca.cs.mcgill.swevo.jayfxbenchmark.enumtest.Planet.mass()" , false)));
    	assertTrue( lRange.contains( getElement( "ca.cs.mcgill.swevo.jayfxbenchmark.enumtest.Planet.G" , false)));
    	assertTrue( lRange.contains( getElement( "ca.cs.mcgill.swevo.jayfxbenchmark.enumtest.Planet.<clinit>()" , false)));
    	assertFalse( lRange.contains( getElement( "ca.cs.mcgill.swevo.jayfxbenchmark.enumtest.Planet.<init>()" , false)));
    	
    	lRange = aDB.getRange( getElement( "ca.cs.mcgill.swevo.jayfxbenchmark.enumtest.Planet$Composition", true), Relation.DECLARES );
    	assertEquals( 3, lRange.size() );
    	assertFalse( lRange.contains( getElement( "ca.cs.mcgill.swevo.jayfxbenchmark.enumtest.Planet$Composition.SOLID", false)));
    	
    	lRange = aDB.getRange( getElement( "ca.cs.mcgill.swevo.jayfxbenchmark.enumtest.Card", true), Relation.DECLARES );
    	assertEquals( 5, lRange.size() );
    	assertFalse( lRange.contains( getElement( "ca.cs.mcgill.swevo.jayfxbenchmark.enumtest.Planet.<clinit>()" , false)));
    }
    
    public void testEnumAccess()
    {
    	Set lRange = aDB.getRange( getElement( "ca.cs.mcgill.swevo.jayfxbenchmark.enumtest.Planet.<init>(D,D,Lca.cs.mcgill.swevo.jayfxbenchmark.enumtest.Planet$Composition;)", false), Relation.ACCESSES );

    	assertEquals( 3, lRange.size() );
    	assertTrue( lRange.contains( getElement( "ca.cs.mcgill.swevo.jayfxbenchmark.enumtest.Planet.mass" , false)));
    }
    
    
    public void testEnumConstantCreate()
    {
    	try {
    		Set lRange = aDB.getRange( getElement( "ca.cs.mcgill.swevo.jayfxbenchmark.enumtest.Planet.<init>(D,D,Lca.cs.mcgill.swevo.jayfxbenchmark.enumtest.Planet$Composition;)", false), Relation.CREATES );
    		throw new AssertionFailedError( "Previously unsupported relationship now found!" );
    	}
    	catch( RelationNotSupportedException e ) {	}
    }

    
    public void testEnumTAccess()
    {
    	Set lRange = aDB.getRange( getElement( "ca.cs.mcgill.swevo.jayfxbenchmark.enumtest.Planet.JUPITER" , false), Relation.T_ACCESSES );
//    	for (Object i : lRange)
//    	{
//    		System.out.println(i.toString());
//    	}
//    	System.out.println();
//    	for (Object i : lRange)
//    	{
//    		System.out.println(i.toString());
//    	}
    	//assertEquals( 1, lRange.size() ); // not sure yet...
    	assertTrue( lRange.contains( getElement( "ca.cs.mcgill.swevo.jayfxbenchmark.enumtest.Planet.getLargestPlanet()" , false)));
    	
//    	lRange = aDB.getRange( getElement( "ca.cs.mcgill.swevo.jayfxbenchmark.enumtest.Planet$Composition.ROCK" , false), Relation.T_ACCESSES );
//    	System.out.println();
//    	for (Object i : lRange)
//    	{
//    		System.out.println(i.toString());
//    	}
//    	assertEquals( 4, lRange.size() );
    }
    
    public void testImportStatic()
    {
    	Set lRange = aDB.getRange( getElement( "ca.cs.mcgill.swevo.jayfxbenchmark.generictest.TLA_QandA.answer(Ljava.lang.String;)" , false), Relation.ACCESSES );
    	assertEquals( 3, lRange.size() );
    	assertTrue( lRange.contains( getElement( "ca.cs.mcgill.swevo.jayfxbenchmark.generictest.TLA.FUD" , false)));
    	assertTrue( lRange.contains( getElement( "ca.cs.mcgill.swevo.jayfxbenchmark.generictest.TLA.IBM" , false)));    	
    }

}
