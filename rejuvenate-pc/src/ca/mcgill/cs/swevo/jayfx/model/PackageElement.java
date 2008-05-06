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

import org.eclipse.jdt.core.IJavaElement;

/**
 * Represents a class program element.
 */
public class PackageElement extends AbstractElement
{
	private IJavaElement elem;
	
	/** Initialize a class element with its fully qualified name 
	 * Class elements should only be created by a FlyweightElementFactory.
	 * @param pId The fully qualified name of the class.
	 */
	protected PackageElement(String pId, IJavaElement elem)
	{
		super( pId );
		this.elem = elem;
	}
	
	/** Returns the category of this element, which always a class.
	 * @return the keyword "class".
	 */
	public ICategories getCategory()
	{
		return ICategories.PACKAGE;
	}
	
	/**
	 * @param pObject The object to compare the class to.
	 * @return Whether pObject has the same ID as this element.
	 */
	public boolean equals( Object pObject )
	{
		if( !(pObject instanceof PackageElement))
			return false;
		else
			return getId().equals(((PackageElement)pObject).getId() );
	}
	
	/** 
	 * @return A hash code for this element.
	 */
	public int hashCode()
	{
		return getId().hashCode();
	}
	
	/** 
	 * @return The declaring class of this class.  null is the
	 * element is a top-level class.
	 */
	public ClassElement getDeclaringClass()
	{
		return null;
	}
	
	/** 
	 * @return The name of the package in which this class is defined.
	 */
	public String getPackageName()
	{
		return this.getId();
//		int lIndex = getId().lastIndexOf( "." );
//		if( lIndex >= 0 )
//			return getId().substring(0, getId().lastIndexOf("."));
//		else
//			return "";
	}
	
	/**
	 * @return The name of the class without the package prefix.
	 */
	public String getShortName()
	{
//		String lPackageName = getPackageName();
//		if( lPackageName.length() > 0 )
//			return getId().substring( lPackageName.length() +1, getId().length() );
//		else
			return getId();
	}

	/* (non-Javadoc)
	 * @see ca.mcgill.cs.swevo.jayfx.model.AbstractElement#getJavaElement()
	 */
	@Override
	public IJavaElement getJavaElement() {
		return this.elem;
	}
	
	private boolean enabled;

	/* (non-Javadoc)
	 * @see ca.mcgill.cs.swevo.jayfx.model.IElement#enable()
	 */
	public void enable() {
		this.enabled = true;
	}

	/* (non-Javadoc)
	 * @see ca.mcgill.cs.swevo.jayfx.model.IElement#isEnabled()
	 */
	public boolean isEnabled() {
		return this.enabled;
	}
	
	/* (non-Javadoc)
	 * @see ca.mcgill.cs.swevo.jayfx.model.IElement#hasEnabledRelationFor(ca.mcgill.cs.swevo.jayfx.model.Relation)
	 */
	public boolean hasEnabledRelationFor(Relation relation) {
		return this.enabledIncommingRelations.contains(relation);
	}
	
	Set<Relation> enabledIncommingRelations = new HashSet<Relation>();

	/* (non-Javadoc)
	 * @see ca.mcgill.cs.swevo.jayfx.model.IElement#enableIncommingRelationsFor(ca.mcgill.cs.swevo.jayfx.model.Relation)
	 */
	public void enableIncommingRelationsFor(Relation relation) {
		this.enabledIncommingRelations.add(relation);
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
}