/* JayFX - A Fact Extractor Plug-in for Eclipse
 * Copyright (C) 2006  McGill University (http://www.cs.mcgill.ca/~swevo/jayfx)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * $Revision: 1.8 $
 */

package ca.mcgill.cs.swevo.jayfx.model;

import java.util.Hashtable;

import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.core.IJavaElement;

import uk.ac.lancs.comp.khatchad.ajayfx.model.AJCodeElement;
import uk.ac.lancs.comp.khatchad.ajayfx.model.AdviceElement;
import uk.ac.lancs.comp.khatchad.ajayfx.model.AspectElement;

/**
 * Factory participant in the Flyweight design pattern.  Produces unique
 * IElement objects representing the various elements in a Java program.
 */
public class FlyweightElementFactory
{
	
	private FlyweightElementFactory() {
		
	}
	
	private static final String KEY_SEPARATOR = ":";
	
	private static Hashtable<String, IElement> aElements = new Hashtable<String, IElement>();
	
	/** 
	 * Returns a flyweight object representing a program element.
	 * @param pCategory The category of element.  Must be a value 
	 * declared in ICategories.
	 * @param pId The id for the element.  For example, a field Id for 
	 * ICategories.FIELD.
	 * @see <a href="http://java.sun.com/docs/books/jls/third_edition/html/binaryComp.html#13.1">
	 * Java Specification, Third Section, 13.1 Section for the binary name convention</a> 
	 * @return A flyweight IElement.
	 * @exception InternalProblemException if an invalid category is passed as parameter.
	 */
	public static IElement getElement( ICategories pCategory, String pId, IJavaElement elem)
	{
		IElement lReturn = (IElement) aElements.get( pCategory + KEY_SEPARATOR + pId );
		if( lReturn == null )
		{	
			if( pCategory == ICategories.CLASS )
			{
				lReturn = new ClassElement( pId );
			}
			else if( pCategory == ICategories.FIELD )
			{
				lReturn = new FieldElement( pId );
			}
			else if( pCategory == ICategories.METHOD )
			{
				lReturn = new MethodElement( pId );
			}
			else if(pCategory == ICategories.PACKAGE) {
				lReturn = new PackageElement(pId, elem);
			}
			else if (pCategory == ICategories.ASPECT) {
				lReturn = new AspectElement(pId);
			}
			else if (pCategory == ICategories.ADVICE) {
				lReturn = new AdviceElement(pId);
			}
			else if (pCategory == ICategories.AJCODE) {
				lReturn = new AJCodeElement(pId);
			}
			else
			{
				throw new InternalProblemException( "Invalid element category: " + pCategory );
			}
			aElements.put( pCategory + KEY_SEPARATOR + pId, lReturn );
		}
		return lReturn;
	}
}