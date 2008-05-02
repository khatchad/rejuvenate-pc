/**
 * 
 */
package uk.ac.lancs.comp.khatchad.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Stack;

import org.drools.ObjectFilter;

import ca.mcgill.cs.swevo.jayfx.model.IElement;

import uk.ac.lancs.khatchad.IntentionEdge;
import uk.ac.lancs.khatchad.IntentionNode;

/**
 * @author raffi
 * 
 */
public class Path<E extends IntentionEdge<IElement>> extends Stack<E> {

	public Path<IntentionEdge<IElement>> extractPattern(
			IntentionNode<IElement> commonNode) {
		Path<IntentionEdge<IElement>> ret = new Path<IntentionEdge<IElement>>();
		for (IntentionEdge<IElement> edge : this) {
			if (edge.getFromNode().equals(commonNode)) {
				IntentionEdge<IElement> newEdge = new IntentionEdge<IElement>(
						edge.getFromNode(),
						edge.getToNode().isEnabled() ? IntentionNode.ENABLED_WILDCARD
								: IntentionNode.DISABLED_WILDCARD, edge
								.getType());
				ret.add(newEdge);
			} else if (edge.getToNode().equals(commonNode)) {
				IntentionEdge<IElement> newEdge = new IntentionEdge<IElement>(
						edge.getFromNode().isEnabled() ? IntentionNode.ENABLED_WILDCARD
								: IntentionNode.DISABLED_WILDCARD, edge
								.getToNode(), edge.getType());
				ret.add(newEdge);
			} else {
				IntentionEdge<IElement> newEdge = new IntentionEdge<IElement>(
						edge.getFromNode().isEnabled() ? IntentionNode.ENABLED_WILDCARD
								: IntentionNode.DISABLED_WILDCARD,
						edge.getToNode().isEnabled() ? IntentionNode.ENABLED_WILDCARD
								: IntentionNode.DISABLED_WILDCARD, edge
								.getType());
				ret.add(newEdge);
			}
		}
		return ret;
	}

	@Override
	public E push(E o) {
		if (!this.isEmpty() && !this.getTopNode().equals(o.getToNode()))
			throw new IllegalArgumentException("Not connectable: " + o);
		return super.push(o);
	}

	public IntentionNode<?> getFirstNode() {
		return this.firstElement().getFromNode();
	}

	public IntentionNode<?> getLastNode() {
		return this.lastElement().getToNode();
	}

	public IntentionNode<?> getTopNode() {
		return this.peek().getFromNode();
	}

	public Collection<IntentionNode<?>> getTailNodes() {
		Collection<IntentionNode<?>> ret = new ArrayList<IntentionNode<?>>();

		if (!this.isEmpty())
			ret.add(this.get(0).getToNode());

		for (int i = 1; i < this.size(); i++) {
			IntentionEdge<?> edge = this.get(i);
			ret.add(edge.getFromNode());
			ret.add(edge.getToNode());
		}

		return ret;
	}

	public PathElements<IntentionNode<?>> getPathElements() {
		PathElements<IntentionNode<?>> ret = new PathElements<IntentionNode<?>>(
				this);
		ret.add(this.getTopNode());
		ret.addAll(this.getTailNodes());
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Vector#toString()
	 */
	@Override
	public synchronized String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("[");

		if (!this.isEmpty()) {
			buf.append(String.valueOf(this.get(0).getFromNode()));
			buf.append(", ");
		}

		Iterator<E> i = iterator();
		boolean hasNext = i.hasNext();
		while (hasNext) {
			E o = i.next();
			buf.append(String.valueOf(o));
			buf.append(", ");
			hasNext = i.hasNext();
			buf.append(o.getToNode());

			if (hasNext) {
				buf.append(", ");
			}
		}

		buf.append("]");
		return buf.toString();
	}

	public static class PathObjectFilter implements ObjectFilter {

		/* (non-Javadoc)
		 * @see org.drools.ObjectFilter#accept(java.lang.Object)
		 */
		public boolean accept(Object object) {
			return object instanceof Path;
		}
	}

	public IntentionEdge<?>[] getEdges() {
		IntentionEdge<?>[] ret = new IntentionEdge[this.size()];
		return this.toArray(ret);
	}

	public Collection<IntentionNode<IElement>> getNodes() {
		Collection<IntentionNode<IElement>> ret = new LinkedHashSet<IntentionNode<IElement>>();
		for (IntentionEdge<IElement> edge : this) {
			ret.add(edge.getFromNode());
			ret.add(edge.getToNode());
		}
		return ret;
	}

	public boolean typeEquivalent(Path<E> rhs) {
		if (this.size() != rhs.size())
			return false;
		for (int i = 0; i < this.size(); i++)
			if (!this.get(i).getType().equals(rhs.get(i).getType()))
				return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.util.Vector#equals(java.lang.Object)
	 */
	@Override
	public synchronized boolean equals(Object o) {
		// TODO Auto-generated method stub
		return super.equals(o);
	}

	/* (non-Javadoc)
	 * @see java.util.Vector#hashCode()
	 */
	@Override
	public synchronized int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}
}