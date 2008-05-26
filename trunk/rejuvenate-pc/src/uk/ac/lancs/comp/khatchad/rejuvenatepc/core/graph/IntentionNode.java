/**
 * 
 */
package uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph;

import java.util.HashSet;
import java.util.Set;

import uk.ac.lancs.comp.khatchad.ajayfx.model.NullElement;
import ca.mcgill.cs.swevo.jayfx.model.IElement;
import ca.mcgill.cs.swevo.jayfx.model.Relation;

/**
 * @author raffi
 * 
 */
public class IntentionNode<E extends IElement> extends IntentionElement<E> {

	public static final IntentionNode<IElement> DISABLED_WILDCARD = new IntentionNode<IElement>(
			new WildcardElement());

	public static final IntentionNode<IElement> ENABLED_WILDCARD = new IntentionNode<IElement>(
			new WildcardElement(true));

	public static final IntentionNode<IElement> NULL = new IntentionNode<IElement>(
			new NullElement());

	private final E elem;
	private final Set<IntentionEdge<E>> edges = new HashSet<IntentionEdge<E>>();

	/**
	 * @param elem
	 */
	public IntentionNode(final E elem) {
		this.elem = elem;
		if (elem.isEnabled())
			this.enable();
		else
			this.disable();
	}

	/**
	 * @param intentionNode
	 */
	public void addEdge(final IntentionEdge<E> intentionEdge) {
		this.edges.add(intentionEdge);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(final Object obj) {
		return obj instanceof IntentionNode ? this.elem
				.equals(((IntentionNode) obj).elem) : false;
	}

	/**
	 * @return the edges
	 */
	public Set<IntentionEdge<E>> getEdges() {
		return this.edges;
	}

	/**
	 * @return the elem
	 */
	public E getElem() {
		return this.elem;
	}

	/**
	 * @param advises
	 * @return
	 */
	public boolean hasEdge(final Relation relation) {
		for (final IntentionEdge<E> edge : this.edges)
			if (edge.getType().equals(relation))
				return true;
		return false;
	}

	/**
	 * @param relation
	 * @return
	 */
	public boolean hasEnabledEdgesForIncommingRelation(final Relation relation) {
		return this.elem.hasEnabledRelationFor(relation);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.elem.hashCode();
	}

	public String toDotFormat() {
		final StringBuilder ret = new StringBuilder();
		ret.append(this.hashCode());
		ret.append(" [label=\"");
		ret.append(this.elem.getShortName());
		ret.append('"');
		if (this.isEnabled())
			ret.append(",style=filled,color=red,fontcolor=white");
		ret.append("];");

		for (final IntentionEdge<E> edge : this.edges)
			ret.append(edge.toDotFormat());
		return ret.toString();
	}

	@Override
	public String toString() {
		final StringBuilder ret = new StringBuilder();
		//		ret.append('(');
		ret.append(super.toString());
		ret.append(this.elem.getShortName());
		//		ret.append(')');
		return ret.toString();
	}

}