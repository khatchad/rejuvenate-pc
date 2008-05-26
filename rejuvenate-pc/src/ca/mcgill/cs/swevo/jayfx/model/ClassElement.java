/* JayFX - A Fact Extractor Plug-in for Eclipse
 * Copyright (C) 2006  McGill University (http://www.cs.mcgill.ca/~swevo/jayfx)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * $Revision: 1.5 $
 */

package ca.mcgill.cs.swevo.jayfx.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a class program element.
 */
public class ClassElement extends AbstractElement {
	private boolean enabled;

	Set<Relation> enabledIncommingRelations = new HashSet<Relation>();

	/**
	 * Initialize a class element with its fully qualified name Class elements
	 * should only be created by a FlyweightElementFactory.
	 * 
	 * @param pId
	 *            The fully qualified name of the class.
	 */
	protected ClassElement(final String pId) {
		super(pId);
	}

	/* (non-Javadoc)
	 * @see ca.mcgill.cs.swevo.jayfx.model.IElement#disable()
	 */
	public void disable() {
		this.enabled = false;
	}

	/* (non-Javadoc)
	 * @see ca.mcgill.cs.swevo.jayfx.model.IElement#disableAllIncommingRelations()
	 */
	public void disableAllIncommingRelations() {
		this.enabledIncommingRelations.clear();
	}

	/* (non-Javadoc)
	 * @see ca.mcgill.cs.swevo.jayfx.model.IElement#enable()
	 */
	public void enable() {
		this.enabled = true;
	}

	/* (non-Javadoc)
	 * @see ca.mcgill.cs.swevo.jayfx.model.IElement#enableIncommingRelationsFor(ca.mcgill.cs.swevo.jayfx.model.Relation)
	 */
	public void enableIncommingRelationsFor(final Relation relation) {
		this.enabledIncommingRelations.add(relation);
	}

	/**
	 * @param pObject
	 *            The object to compare the class to.
	 * @return Whether pObject has the same ID as this element.
	 */
	@Override
	public boolean equals(final Object pObject) {
		if (!(pObject instanceof ClassElement))
			return false;
		else
			return this.getId().equals(((ClassElement) pObject).getId());
	}

	/**
	 * Returns the category of this element, which always a class.
	 * 
	 * @return the keyword "class".
	 */
	@Override
	public ICategories getCategory() {
		return ICategories.CLASS;
	}

	/**
	 * @return The declaring class of this class. null is the element is a
	 *         top-level class.
	 */
	public ClassElement getDeclaringClass() {
		return null;
	}

	/**
	 * @return The name of the package in which this class is defined.
	 */
	public String getPackageName() {
		final int lIndex = this.getId().lastIndexOf(".");
		if (lIndex >= 0)
			return this.getId().substring(0, this.getId().lastIndexOf("."));
		else
			return "";
	}

	/**
	 * @return The name of the class without the package prefix.
	 */
	@Override
	public String getShortName() {
		final String lPackageName = this.getPackageName();
		if (lPackageName.length() > 0)
			return this.getId().substring(lPackageName.length() + 1,
					this.getId().length());
		else
			return this.getId();
	}

	/* (non-Javadoc)
	 * @see ca.mcgill.cs.swevo.jayfx.model.IElement#hasEnabledRelationFor(ca.mcgill.cs.swevo.jayfx.model.Relation)
	 */
	public boolean hasEnabledRelationFor(final Relation relation) {
		return this.enabledIncommingRelations.contains(relation);
	}

	/**
	 * @return A hash code for this element.
	 */
	@Override
	public int hashCode() {
		return this.getId().hashCode();
	}

	/* (non-Javadoc)
	 * @see ca.mcgill.cs.swevo.jayfx.model.IElement#isEnabled()
	 */
	public boolean isEnabled() {
		return this.enabled;
	}
}