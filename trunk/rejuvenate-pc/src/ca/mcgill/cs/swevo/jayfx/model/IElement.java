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

import java.io.Serializable;

import org.eclipse.jdt.core.IJavaElement;

/**
 * A program element in the Java model.
 */
public interface IElement extends Serializable {
	public void disable();

	public void disableAllIncommingRelations();

	/**
	 * 
	 */
	public void enable();

	/**
	 * @param calls
	 */
	public void enableIncommingRelationsFor(Relation calls);

	/**
	 * @return The category for this element.
	 */
	public ICategories getCategory();

	/**
	 * @return The declaring class of the element. Null if the element is a
	 *         top-level class (i.e., not an inner class.
	 */
	public ClassElement getDeclaringClass();

	/**
	 * @return The ID for this element.
	 */
	public String getId();

	public IJavaElement getJavaElement();

	/**
	 * @return String Name of the package this element belongs to.
	 */
	public String getPackageName();

	/**
	 * @return The id of this element without the package.
	 */
	public String getShortName();

	public boolean hasEnabledRelationFor(Relation relation);

	public boolean isEnabled();
}