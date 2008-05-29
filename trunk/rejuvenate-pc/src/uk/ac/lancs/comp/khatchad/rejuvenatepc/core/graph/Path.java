/**
 * 
 */
package uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Stack;

import org.drools.ObjectFilter;
import org.jdom.Element;

import ca.mcgill.cs.swevo.jayfx.model.FlyweightElementFactory;
import ca.mcgill.cs.swevo.jayfx.model.IElement;

/**
 * @author raffi
 * 
 */
public class Path<E extends IntentionEdge<IElement>> extends Stack<E> implements Serializable {

	public static class PathObjectFilter implements ObjectFilter {

		/* (non-Javadoc)
		 * @see org.drools.ObjectFilter#accept(java.lang.Object)
		 */
		public boolean accept(final Object object) {
			return object instanceof Path;
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -456698317927297139L;

	/* (non-Javadoc)
	 * @see java.util.Vector#equals(java.lang.Object)
	 */
	@Override
	public synchronized boolean equals(final Object o) {
		// TODO Auto-generated method stub
		return super.equals(o);
	}

	public Pattern<IntentionEdge<IElement>> extractPattern(
			final IntentionNode<IElement> commonNode, IntentionNode<IElement> enabledNode) {
		
		final Pattern<IntentionEdge<IElement>> ret = new Pattern<IntentionEdge<IElement>>();
		for (final IntentionEdge<IElement> edge : this)
			if (edge.getFromNode().equals(commonNode)) {
				final IntentionEdge<IElement> newEdge = new IntentionEdge<IElement>(
						edge.getFromNode(),
						edge.getToNode().equals(enabledNode) ? IntentionNode.ENABLED_WILDCARD
								: IntentionNode.DISABLED_WILDCARD, edge
								.getType(), false);
				ret.add(newEdge);
			}
			else if (edge.getToNode().equals(commonNode)) {
				final IntentionEdge<IElement> newEdge = new IntentionEdge<IElement>(
						edge.getFromNode().equals(enabledNode) ? IntentionNode.ENABLED_WILDCARD
								: IntentionNode.DISABLED_WILDCARD, edge
								.getToNode(), edge.getType(), false);
				ret.add(newEdge);
			}
			else {
				final IntentionEdge<IElement> newEdge = new IntentionEdge<IElement>(
						edge.getFromNode().equals(enabledNode) ? IntentionNode.ENABLED_WILDCARD
								: IntentionNode.DISABLED_WILDCARD,
						edge.getToNode().equals(enabledNode) ? IntentionNode.ENABLED_WILDCARD
								: IntentionNode.DISABLED_WILDCARD, edge
								.getType(), false);
				ret.add(newEdge);
			}
		return ret;
	}

	public IntentionEdge<?>[] getEdges() {
		final IntentionEdge<?>[] ret = new IntentionEdge[this.size()];
		return this.toArray(ret);
	}

	public IntentionEdge<?> getFirstEdge() {
		return this.firstElement();
	}

	public IntentionNode<?> getFirstNode() {
		return this.firstElement().getFromNode();
	}

	public IntentionEdge<?> getLastEdge() {
		return this.lastElement();
	}

	public IntentionNode<?> getLastNode() {
		return this.lastElement().getToNode();
	}

	public Collection<IntentionNode<IElement>> getNodes() {
		final Collection<IntentionNode<IElement>> ret = new ArrayList<IntentionNode<IElement>>();
		if (!this.isEmpty()) {
			ret.add(this.firstElement().getFromNode());
			for (final IntentionEdge<IElement> edge : this)
				ret.add(edge.getToNode());
		}
		return ret;
	}

	public PathElements<IntentionNode<?>> getPathElements() {
		final PathElements<IntentionNode<?>> ret = new PathElements<IntentionNode<?>>(
				this);
		ret.add(this.getTopNode());
		ret.addAll(this.getTailNodes());
		return ret;
	}

	public Collection<IntentionNode<?>> getTailNodes() {
		final Collection<IntentionNode<?>> ret = new ArrayList<IntentionNode<?>>();

		if (!this.isEmpty())
			ret.add(this.get(0).getToNode());

		for (int i = 1; i < this.size(); i++) {
			final IntentionEdge<?> edge = this.get(i);
			ret.add(edge.getFromNode());
			ret.add(edge.getToNode());
		}

		return ret;
	}

	public IntentionNode<?> getTopNode() {
		return this.peek().getFromNode();
	}

	public Collection<IntentionNode<IElement>> getWildcardNodes() {
		final Collection<IntentionNode<IElement>> ret = this.getNodes();
		for (final Iterator<IntentionNode<IElement>> it = ret.iterator(); it
				.hasNext();) {
			final IntentionNode<IElement> node = it.next();
			if (!(node.getElem() instanceof WildcardElement))
				it.remove();
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see java.util.Vector#hashCode()
	 */
	@Override
	public synchronized int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}

	@Override
	public E push(final E o) {
		if (!this.isEmpty() && !this.getTopNode().equals(o.getToNode()))
			throw new IllegalArgumentException("Not connectable: " + o);
		return super.push(o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Vector#toString()
	 */
	@Override
	public synchronized String toString() {
		final StringBuilder buf = new StringBuilder();
		buf.append("[");

		if (!this.isEmpty()) {
			buf.append(String.valueOf(this.get(0).getFromNode()));
			buf.append(", ");
		}

		final Iterator<E> i = this.iterator();
		boolean hasNext = i.hasNext();
		while (hasNext) {
			final E o = i.next();
			buf.append(String.valueOf(o));
			buf.append(", ");
			hasNext = i.hasNext();
			buf.append(o.getToNode());

			if (hasNext)
				buf.append(", ");
		}

		buf.append("]");
		return buf.toString();
	}

	public boolean typeEquivalent(final Path<E> rhs) {
		if (this.size() != rhs.size())
			return false;
		for (int i = 0; i < this.size(); i++)
			if (!this.get(i).getType().equals(rhs.get(i).getType()))
				return false;
		return true;
	}
	
	public Element getXML() {
		Element root = new Element(this.getClass().getSimpleName());

		if (!this.isEmpty()) {
			root.addContent(this.get(0).getFromNode().getXML());
		}

		final Iterator<E> i = this.iterator();
		boolean hasNext = i.hasNext();
		while (hasNext) {
			final E o = i.next();
			root.addContent(o.getXML());
			root.addContent((o.getToNode().getXML()));
			hasNext = i.hasNext();
		}

		return root;
	}


	/**
	 * @param commonNode
	 * @param enabledEdge
	 * @return
	 */
	public Pattern extractPattern(IntentionNode commonNode,
			IntentionElement enabledEdge) {
		// TODO Auto-generated method stub
		return null;
	}
}