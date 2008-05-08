/**
 * 
 */
package uk.ac.lancs.comp.khatchad.core;

import org.eclipse.jdt.core.IJavaElement;

import ca.mcgill.cs.swevo.jayfx.model.ClassElement;
import ca.mcgill.cs.swevo.jayfx.model.ICategories;
import ca.mcgill.cs.swevo.jayfx.model.IElement;
import ca.mcgill.cs.swevo.jayfx.model.Relation;

/**
 * @author raffi
 *
 */
public class NullElement implements IElement {

	/* (non-Javadoc)
	 * @see ca.mcgill.cs.swevo.jayfx.model.IElement#disable()
	 */
	public void disable() {
	}

	/* (non-Javadoc)
	 * @see ca.mcgill.cs.swevo.jayfx.model.IElement#enable()
	 */
	public void enable() {
	}

	/* (non-Javadoc)
	 * @see ca.mcgill.cs.swevo.jayfx.model.IElement#enableIncommingRelationsFor(ca.mcgill.cs.swevo.jayfx.model.Relation)
	 */
	public void enableIncommingRelationsFor(Relation calls) {
	}

	/* (non-Javadoc)
	 * @see ca.mcgill.cs.swevo.jayfx.model.IElement#getCategory()
	 */
	public ICategories getCategory() {
		return ICategories.NULL;
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
		return "NULL";
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
		return this.getId();
	}

	/* (non-Javadoc)
	 * @see ca.mcgill.cs.swevo.jayfx.model.IElement#hasEnabledRelationFor(ca.mcgill.cs.swevo.jayfx.model.Relation)
	 */
	public boolean hasEnabledRelationFor(Relation relation) {
		return false;
	}

	/* (non-Javadoc)
	 * @see ca.mcgill.cs.swevo.jayfx.model.IElement#isEnabled()
	 */
	public boolean isEnabled() {
		return false;
	}

	/* (non-Javadoc)
	 * @see ca.mcgill.cs.swevo.jayfx.model.IElement#disableAllIncommingRelations()
	 */
	public void disableAllIncommingRelations() {
	}
}