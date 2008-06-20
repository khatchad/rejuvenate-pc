/**
 * 
 */
package uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph;

import java.util.Set;

import org.eclipse.ajdt.core.AspectJCore;
import org.eclipse.ajdt.core.model.AJModel;
import org.eclipse.ajdt.core.model.AJProjectModel;
import org.eclipse.jdt.core.IJavaElement;
import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;

import ca.mcgill.cs.swevo.jayfx.model.IElement;
import ca.mcgill.cs.swevo.jayfx.model.Relation;

/**
 * @author raffi
 * 
 */
public class IntentionEdge<E extends IElement> extends IntentionElement<E> {

	/**
	 * 
	 */
	private static final String SOURCE = "source";

	/**
	 * 
	 */
	private static final String TARGET = "target";

	private static final long serialVersionUID = -4758844315757084370L;

	private IntentionNode<E> fromNode;

	private IntentionNode<E> toNode;

	private Relation type;

	/**
	 * 
	 */
	public IntentionEdge() {
	}

	/**
	 * @param from
	 * @param to
	 * @param type
	 */
	public IntentionEdge(final IntentionNode<E> from,
			final IntentionNode<E> to, final Relation type) {
		this.fromNode = from;
		this.toNode = to;
		this.type = type;
	}

	/**
	 * @param node
	 * @param toNode
	 * @param relation
	 * @param enableEdgesForIncommingRelation
	 */
	public IntentionEdge(final IntentionNode<E> from,
			final IntentionNode<E> to, final Relation type,
			final boolean enabled) {
		this(from, to, type);
		if (enabled)
			this.enable();
		else
			this.disable();
	}

	/**
	 * @param elem
	 * @throws DataConversionException 
	 */
	public IntentionEdge(Element elem) throws DataConversionException {
		super(elem);
		
		Element typeElem = elem.getChild(Relation.class.getSimpleName());
		this.type = Relation.valueOf(typeElem);
		
		Element sourceElem = elem.getChild(SOURCE).getChild(IntentionNode.class.getSimpleName());
		this.fromNode = recoverNode(sourceElem);
		
		Element targetElem = elem.getChild(TARGET).getChild(IntentionNode.class.getSimpleName());
		this.toNode = recoverNode(targetElem);
	}

	/**
	 * @param sourceElem
	 * @throws DataConversionException
	 */
	private static <E extends IElement> IntentionNode<E> recoverNode(Element sourceElem) throws DataConversionException {
		if ( WildcardElement.isWildcardElement(sourceElem.getChild(IElement.class.getSimpleName())) ) 
			return (IntentionNode<E>)(IntentionElement.isEnabled(sourceElem) ? IntentionNode.ENABLED_WILDCARD : IntentionNode.DISABLED_WILDCARD);
		else 
			return new IntentionNode<E>(sourceElem);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(final Object obj) {
		return obj instanceof IntentionEdge ? this.fromNode
				.equals(((IntentionEdge) obj).fromNode)
				&& this.toNode.equals(((IntentionEdge) obj).toNode)
				&& this.type.equals(((IntentionEdge) obj).type) : false;
	}

	/**
	 * @return the from
	 */
	public IntentionNode<E> getFromNode() {
		return this.fromNode;
	}

	/**
	 * @return the to
	 */
	public IntentionNode<E> getToNode() {
		return this.toNode;
	}

	/**
	 * @return the type
	 */
	public Relation getType() {
		return this.type;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.fromNode.hashCode() + this.toNode.hashCode()
				+ this.type.hashCode();
	}

	/**
	 * @param fromNode
	 *            the fromNode to set
	 */
	public void setFromNode(final IntentionNode<E> fromNode) {
		this.fromNode = fromNode;
	}

	/**
	 * @param toNode
	 *            the toNode to set
	 */
	public void setToNode(final IntentionNode<E> toNode) {
		this.toNode = toNode;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(final Relation type) {
		this.type = type;
	}

	public String toDotFormat() {
		final StringBuilder ret = new StringBuilder();
		ret.append(this.fromNode.hashCode());
		ret.append("->");
		ret.append(this.toNode.hashCode());
		ret.append(' ');
		ret.append("[label=");
		ret.append("\"");
		ret.append(this.type.getFullCode());
		ret.append("\"");
		if (this.isEnabled())
			ret.append(",style=bold,color=red,fontcolor=red");
		ret.append("];");
		return ret.toString();
	}

	@Override
	public String toString() {
		final StringBuilder ret = new StringBuilder();
		ret.append(super.toString());
		//		ret.append('(');
		//		ret.append(from.getElem().getShortName());
		//		ret.append(',');
		//		ret.append(to.getElem().getShortName());
		//		ret.append(')');
		ret.append(this.type.getFullCode());
		return ret.toString();
	}

	@Override
	public String getLongDescription() {
		StringBuilder ret = new StringBuilder();
		ret.append(super.toString());
		ret.append(this.type.toString() + ": ");
		ret.append(this.getToNode().getLongDescription());
		return ret.toString();
	}

	public Element getXML() {
		Element ret = super.getXML();
				
		Element typeXML = this.type.getXML();
		ret.addContent(typeXML);
		
		Element source = new Element(SOURCE);
		source.addContent(this.getFromNode().getXML());
		ret.addContent(source);
		
		Element target = new Element(TARGET);
		target.addContent(this.getToNode().getXML());
		ret.addContent(target);
		
		return ret;
	}

	/**
	 * Returns the AJCodeElements corresponding to this edge.
	 * 
	 * @return The AJCodeElements corresponding to this edge.
	 */

	public Set<IJavaElement> getJavaElement() {
		// Need to *somehow* get AJCodeElements here. Perhaps use search engine?
		return null;
	}

	/* (non-Javadoc)
	 * @see uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionElement#getPrettyString()
	 */
	@Override
	public String toPrettyString() {
		StringBuilder ret = new StringBuilder(this.toString().toLowerCase());
		ret.append(": ");
		ret.append(this.toNode);
		return ret.toString();
	}
}