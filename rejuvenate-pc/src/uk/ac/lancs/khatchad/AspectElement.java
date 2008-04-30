/**
 * 
 */
package uk.ac.lancs.khatchad;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import ca.mcgill.cs.swevo.jayfx.model.AbstractElement;
import ca.mcgill.cs.swevo.jayfx.model.ClassElement;
import ca.mcgill.cs.swevo.jayfx.model.ICategories;
import ca.mcgill.cs.swevo.jayfx.model.Relation;

/**
 * @author raffi
 *
 */
public class AspectElement extends AbstractElement {

	/**
	 * @param id
	 */
	public AspectElement(String id) {
		super(id);
	}

	/* (non-Javadoc)
	 * @see ca.mcgill.cs.swevo.jayfx.model.AbstractElement#getCategory()
	 */
	@Override
	public ICategories getCategory() {
		return ICategories.ASPECT;
	}
	
	/* (non-Javadoc)
	 * @see ca.mcgill.cs.swevo.jayfx.model.AbstractElement#getShortName()
	 */
	@Override
	public String getShortName() {
		String lPackageName = getPackageName();
		if( lPackageName.length() > 0 )
			return getId().substring( lPackageName.length() +1, getId().length() );
		else
			return getId();
	}

	/* (non-Javadoc)
	 * @see ca.mcgill.cs.swevo.jayfx.model.IElement#getDeclaringClass()
	 */
	public ClassElement getDeclaringClass() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see ca.mcgill.cs.swevo.jayfx.model.IElement#getPackageName()
	 */
	public String getPackageName() {
		int lIndex = getId().lastIndexOf( "." );
		if( lIndex >= 0 )
			return getId().substring(0, getId().lastIndexOf("."));
		else
			return "";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if( !(obj instanceof AspectElement))
			return false;
		else
			return getId().equals(((AspectElement)obj).getId() );
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.getId().hashCode();
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