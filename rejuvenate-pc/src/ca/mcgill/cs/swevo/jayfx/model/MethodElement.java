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
import java.util.StringTokenizer;

/**
 * Represents a method element in the model.
 */
public class MethodElement extends AbstractElement {
	private boolean enabled;

	Set<Relation> enabledIncommingRelations = new HashSet<Relation>();

	/**
	 * Creates a method objects. Such objects should not be created directly but
	 * should be obtained through a FlyweightElementFactory.
	 * 
	 * @param pId
	 *            The unique descriptor of this method. Comprises the fully
	 *            qualified name of the declaring class, followed by the name of
	 *            the method (or init for constructors), and the parameter list.
	 */
	protected MethodElement(String pId) {
		super(pId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.mcgill.cs.swevo.jayfx.model.IElement#disable()
	 */
	public void disable() {
		this.enabled = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.mcgill.cs.swevo.jayfx.model.IElement#enable()
	 */
	public void enable() {
		this.enabled = true;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.mcgill.cs.swevo.jayfx.model.IElement#enableIncommingRelationsFor(ca.mcgill.cs.swevo.jayfx.model.Relation)
	 */
	public void enableIncommingRelationsFor(Relation relation) {
		this.enabledIncommingRelations.add(relation);
	}

	/**
	 * Equality for method elements is based on the equality of their
	 * corresponding ids.
	 * 
	 * @param pObject
	 *            the object to compare to.
	 * @return true if this object has the same id as pObject.
	 * @see java.lang.Object#equals(Object)
	 */
	@Override
	public boolean equals(Object pObject) {
		if (!(pObject instanceof MethodElement))
			return false;
		else
			return this.getId().equals(((MethodElement) pObject).getId());
	}

	/**
	 * Returns the category of this element type, i.e., a method.
	 * 
	 * @return ICategories.METHOD
	 */
	@Override
	public ICategories getCategory() {
		return ICategories.METHOD;
	}

	/**
	 * @return The name of the class declaring this method.
	 */
	public ClassElement getDeclaringClass() {
		final String lName = this.getFirstParticle();
		final int lIndex = lName.lastIndexOf(".");
		ClassElement lReturn = null;
		lReturn = (ClassElement) FlyweightElementFactory.getElement(
				ICategories.CLASS, lName.substring(0, lIndex), null);
		return lReturn;
	}

	/**
	 * @return The simple name of the method.
	 */
	public String getName() {
		final String lName = this.getFirstParticle();
		final int lIndex = lName.lastIndexOf(".");
		return lName.substring(lIndex + 1, lName.length());
	}

	/**
	 * @return The name of the package in which the declaring class of this
	 *         method is defined in.
	 */
	public String getPackageName() {
		return this.getDeclaringClass().getPackageName();
	}

	/**
	 * @return The String of parameter types for this method, including the
	 *         parentheses.
	 */
	public String getParameters() {
		final int lIndex = this.getId().indexOf("(");
		return this.getId().substring(lIndex, this.getId().length());
	}

	/**
	 * @return The id of this element without the package names for the name of
	 *         the method and the parameter types.
	 */
	@Override
	public String getShortName() {
		String lReturn = this.getDeclaringClass().getShortName() + "."
				+ this.getName() + "(";
		final StringTokenizer lParser = new StringTokenizer(this
				.getParameters(), ",()");
		final int lNbTokens = lParser.countTokens();
		for (int i = 0; i < lNbTokens - 1; i++) {
			final String lToken = lParser.nextToken();
			final int lIndex = lToken.lastIndexOf('.');
			if (lIndex >= 0)
				lReturn += lToken.substring(lIndex + 1, lToken.length()) + ",";
			else
				lReturn += lToken + ",";
		}

		if (lNbTokens > 0) {
			final String lToken = lParser.nextToken();
			final int lIndex = lToken.lastIndexOf('.');
			if (lIndex >= 0)
				lReturn += lToken.substring(lIndex + 1, lToken.length());
			else
				lReturn += lToken;
		}
		return lReturn + ")";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.mcgill.cs.swevo.jayfx.model.IElement#hasEnabledRelationFor(ca.mcgill.cs.swevo.jayfx.model.Relation)
	 */
	public boolean hasEnabledRelationFor(Relation relation) {
		return this.enabledIncommingRelations.contains(relation);
	}

	/**
	 * The hashcode is determined based on the id of the method.
	 * 
	 * @return The hashcode of the id String for this method.
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.getId().hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.mcgill.cs.swevo.jayfx.model.IElement#isEnabled()
	 */
	public boolean isEnabled() {
		return this.enabled;
	}

	/**
	 * @return Fully qualified name of the method.
	 */
	private String getFirstParticle() {
		final int lIndex = this.getId().indexOf("(");
		return this.getId().substring(0, lIndex);
	}
}