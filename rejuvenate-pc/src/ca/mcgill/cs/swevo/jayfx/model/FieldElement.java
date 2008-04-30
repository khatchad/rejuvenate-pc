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

/** Represents a field element in the Java program model.
 */
public class FieldElement extends AbstractElement
{
	/** 
	 * Creates a field element.  This constructor should not
	 * be used directly.  FieldElements should be obtained
	 * through the FlyweightElementFactory.getElement method.
	 * @param pId the Id representing the field, i.e., the fully
	 * qualified name of the declaring class followed by the name of the
	 * field, in dot notation.
	 */
	protected FieldElement( String pId )
	{
		super( pId );
	}
	
	/** 
	 * Returns the category of this element, i.e., a field.
	 * @return ICategories.FIELD.
	 */
	public ICategories getCategory()
	{
		return ICategories.FIELD;
	}
	
	/** 
	 * Determines equality.
	 * @param pObject the object to compare to this object.
	 * @return true if pObject is a Field element with the  same
	 * id as this element.
	 */
	public boolean equals( Object pObject )
	{
		if( !(pObject instanceof FieldElement))
			return false;
		else
			return getId().equals(((FieldElement)pObject).getId() );
	}
	
	/** 
	 * @return a hash code for this object.
	 */
	public int hashCode()
	{
		return getId().hashCode();
	}
	
	/** 
	 * @return The fully-qualified name of the class declaring
	 * this field.
	 */
	public ClassElement getDeclaringClass()
	{
		ClassElement lReturn = null;
		lReturn = (ClassElement)FlyweightElementFactory.getElement( ICategories.CLASS, getId().substring(0, getId().lastIndexOf(".")), null);
		return lReturn;
	}
	
	/** 
	 * @return The name of the package in which the declaring class of this
	 * field is defined in.
	 */
	public String getPackageName()
	{
		return getDeclaringClass().getPackageName();
	}
	
	/**
	 * @return The simple name of the field.
	 */
	public String getSimpleName()
	{
		return getId().substring( getId().lastIndexOf( ".") + 1, getId().length() );
	}
	
	public String getShortName()
	{
		return getDeclaringClass().getShortName() + "." + getSimpleName();
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
}