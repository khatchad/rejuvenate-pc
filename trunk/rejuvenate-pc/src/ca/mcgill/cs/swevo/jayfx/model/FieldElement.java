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
 * Represents a field element in the Java program model.
 */
public class FieldElement extends AbstractElement {
	
	private static final long serialVersionUID = -7741801515115590408L;

	private boolean enabled;

	Set<Relation> enabledIncommingRelations = new HashSet<Relation>();

	/**
	 * Creates a field element. This constructor should not be used directly.
	 * FieldElements should be obtained through the
	 * FlyweightElementFactory.getElement method.
	 * 
	 * @param pId
	 *            the Id representing the field, i.e., the fully qualified name
	 *            of the declaring class followed by the name of the field, in
	 *            dot notation.
	 */
	protected FieldElement(final String pId) {
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
	 * Determines equality.
	 * 
	 * @param pObject
	 *            the object to compare to this object.
	 * @return true if pObject is a Field element with the same id as this
	 *         element.
	 */
	@Override
	public boolean equals(final Object pObject) {
		if (!(pObject instanceof FieldElement))
			return false;
		else
			return this.getId().equals(((FieldElement) pObject).getId());
	}

	/**
	 * Returns the category of this element, i.e., a field.
	 * 
	 * @return Category.FIELD.
	 */
	@Override
	public Category getCategory() {
		return Category.FIELD;
	}

	/**
	 * @return The fully-qualified name of the class declaring this field.
	 */
	public ClassElement getDeclaringClass() {
		ClassElement lReturn = null;
		lReturn = (ClassElement) FlyweightElementFactory.getElement(
				Category.CLASS, this.getId().substring(0,
						this.getId().lastIndexOf(".")));
		return lReturn;
	}

	/**
	 * @return The name of the package in which the declaring class of this
	 *         field is defined in.
	 */
	public String getPackageName() {
		return this.getDeclaringClass().getPackageName();
	}

	@Override
	public String getShortName() {
		return this.getDeclaringClass().getShortName() + "."
				+ this.getSimpleName();
	}

	/**
	 * @return The simple name of the field.
	 */
	public String getSimpleName() {
		return this.getId().substring(this.getId().lastIndexOf(".") + 1,
				this.getId().length());
	}

	/* (non-Javadoc)
	 * @see ca.mcgill.cs.swevo.jayfx.model.IElement#hasEnabledRelationFor(ca.mcgill.cs.swevo.jayfx.model.Relation)
	 */
	public boolean hasEnabledRelationFor(final Relation relation) {
		return this.enabledIncommingRelations.contains(relation);
	}

	/**
	 * @return a hash code for this object.
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