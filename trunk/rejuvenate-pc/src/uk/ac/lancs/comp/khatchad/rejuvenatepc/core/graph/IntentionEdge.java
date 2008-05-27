/**
 * 
 */
package uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph;

import org.jdom.Element;

import ca.mcgill.cs.swevo.jayfx.model.IElement;
import ca.mcgill.cs.swevo.jayfx.model.Relation;

/**
 * @author raffi
 * 
 */
public class IntentionEdge<E extends IElement> extends IntentionElement<E> {
	
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
		ret.append(this.type);
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
		ret.append(this.type);
		return ret.toString();
	}
	
	public Element getXML() {
		Element ret = new Element(this.getClass().getSimpleName());
		ret.setAttribute("enabled", String.valueOf(this.isEnabled()));
		ret.addContent(this.type.getXML());
		return ret;
	}
}