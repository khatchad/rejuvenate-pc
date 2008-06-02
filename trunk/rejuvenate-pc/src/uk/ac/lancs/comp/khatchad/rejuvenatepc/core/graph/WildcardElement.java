/**
 * 
 */
package uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph;

import org.eclipse.jdt.core.IJavaElement;

import ca.mcgill.cs.swevo.jayfx.model.ClassElement;
import ca.mcgill.cs.swevo.jayfx.model.ICategories;
import ca.mcgill.cs.swevo.jayfx.model.IElement;
import ca.mcgill.cs.swevo.jayfx.model.Relation;

/**
 * @author raffi
 * 
 */
public class WildcardElement implements IElement {

	private static final long serialVersionUID = -4175380054692252185L;
	
	private boolean enabled;

	public WildcardElement() {
	}

	public WildcardElement(final boolean enabled) {
		this.enabled = enabled;
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
	}

	/* (non-Javadoc)
	 * @see ca.mcgill.cs.swevo.jayfx.model.IElement#enable()
	 */
	public void enable() {
		this.enabled = true;
	}

	public void enableIncommingRelationsFor(final Relation calls) {
	}

	public ICategories getCategory() {
		return ICategories.WILDCARD;
	}

	/* (non-Javadoc)
	 * @see ca.mcgill.cs.swevo.jayfx.model.IElement#getDeclaringClass()
	 */
	public ClassElement getDeclaringClass() {
		return null;
	}

	/* (non-Javadoc)
	 * @see ca.mcgill.cs.swevo.jayfx.model.IElement#getId()
	 */
	public String getId() {
		return "?";
	}

	/* (non-Javadoc)
	 * @see ca.mcgill.cs.swevo.jayfx.model.IElement#getJavaElement()
	 */
	public IJavaElement getJavaElement() {
		return null;
	}

	/* (non-Javadoc)
	 * @see ca.mcgill.cs.swevo.jayfx.model.IElement#getPackageName()
	 */
	public String getPackageName() {
		return "";
	}

	/* (non-Javadoc)
	 * @see ca.mcgill.cs.swevo.jayfx.model.IElement#getShortName()
	 */
	public String getShortName() {
		return "?";
	}

	/* (non-Javadoc)
	 * @see ca.mcgill.cs.swevo.jayfx.model.IElement#hasEnabledRelationFor(ca.mcgill.cs.swevo.jayfx.model.Relation)
	 */
	public boolean hasEnabledRelationFor(final Relation relation) {
		return false;
	}

	/* (non-Javadoc)
	 * @see ca.mcgill.cs.swevo.jayfx.model.IElement#isEnabled()
	 */
	public boolean isEnabled() {
		return this.enabled;
	}
}