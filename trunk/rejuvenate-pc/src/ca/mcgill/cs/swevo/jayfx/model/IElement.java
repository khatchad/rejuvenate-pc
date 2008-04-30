/* JayFX - A Fact Extractor Plug-in for Eclipse
 * Copyright (C) 2006  McGill University (http://www.cs.mcgill.ca/~swevo/jayfx)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * $Revision: 1.4 $
 */

package ca.mcgill.cs.swevo.jayfx.model;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;

/** 
 * A program element in the Java model.
 */
public interface IElement
{
	/**
	 * @return The category for this element.
	 */
	public ICategories getCategory();
	
	/**
	 * @return The ID for this element.
	 */
	public String getId();
	
	/**
	 * @return The declaring class of the element.
	 * Null if the element is a top-level class (i.e., not
	 * an inner class.
	 */
	public ClassElement getDeclaringClass();
	
	/** 
	 * @return The id of this element without the package.
	 */
	public String getShortName();
	
	/**
	 * @return String Name of the package this element belongs to.
	 */
	public String getPackageName();
	
	public IJavaElement getJavaElement();

	/**
	 * 
	 */
	public void enable();
	
	public void disable();
	
	public boolean isEnabled();
	
	public boolean hasEnabledRelationFor(Relation relation);

	/**
	 * @param calls
	 */
	public void enableIncommingRelationsFor(Relation calls);
}