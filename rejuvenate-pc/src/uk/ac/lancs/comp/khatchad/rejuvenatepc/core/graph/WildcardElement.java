/**
 * 
 */
package uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph;

import org.eclipse.jdt.core.IJavaElement;
import org.jdom.Attribute;
import org.jdom.Element;

import ca.mcgill.cs.swevo.jayfx.model.ClassElement;
import ca.mcgill.cs.swevo.jayfx.model.Category;
import ca.mcgill.cs.swevo.jayfx.model.IElement;
import ca.mcgill.cs.swevo.jayfx.model.Relation;

/**
 * @author raffi
 * 
 */
public class WildcardElement implements IElement {

	/**
	 * 
	 */
	private static final String QUESTION_MARK = "?";

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

	public Category getCategory() {
		return Category.WILDCARD;
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
		return QUESTION_MARK;
	}
	
	public static boolean isWildcardIdentifier(String identifier) {
		return identifier.equals(QUESTION_MARK);
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
		return QUESTION_MARK;
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
	
	public Element getXML() {
		Element ret = new Element(IElement.class.getSimpleName());
		ret.setAttribute(new Attribute(ID, this.getId()));
		ret.addContent(this.getCategory().getXML());
		return ret;
	}

	/**
	 * @param elementXML
	 * @return
	 */
	public static boolean isWildcardElement(Element elementXML) {
		return elementXML.getAttribute(ID).getValue().equals(QUESTION_MARK);
	}
}
