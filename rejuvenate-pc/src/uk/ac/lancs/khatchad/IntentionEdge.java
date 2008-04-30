/**
 * 
 */
package uk.ac.lancs.khatchad;

import ca.mcgill.cs.swevo.jayfx.model.IElement;
import ca.mcgill.cs.swevo.jayfx.model.Relation;

/**
 * @author raffi
 *
 */
public class IntentionEdge<E extends IElement> extends IntentionElement<E> {
	private IntentionNode<E> from;
	
	private IntentionNode<E> to;
	
	private Relation type;
	
	/**
	 * @param from
	 * @param to
	 * @param type
	 */
	public IntentionEdge(IntentionNode<E> from, IntentionNode<E> to, Relation type) {
		this.from = from;
		this.to = to;
		this.type = type;
	}
	
	/**
	 * @param node
	 * @param toNode
	 * @param relation
	 * @param enableEdgesForIncommingRelation
	 */
	public IntentionEdge(IntentionNode<E> from,
			IntentionNode<E> to, Relation type,
			boolean enabled) {
		this(from, to, type);
		if ( enabled )
			this.enable();
		else
			this.disable();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return obj instanceof IntentionEdge ? this.from.equals(((IntentionEdge)obj).from) &&
				this.to.equals(((IntentionEdge)obj).to) && this.type.equals(((IntentionEdge)obj).type) : false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.from.hashCode() + this.to.hashCode() + this.type.hashCode();
	}

	public String toDotFormat() {
		StringBuilder ret = new StringBuilder();
		ret.append(from.hashCode());
		ret.append("->"); 
		ret.append(to.hashCode()); 
		ret.append(' ');
		ret.append("[label=");
		ret.append("\"");
		ret.append(this.type);
		ret.append("\"");
		if ( this.isEnabled() )
			ret.append(",style=bold,color=red,fontcolor=red");
		ret.append("];");
		return ret.toString();
	}

	/**
	 * @return the type
	 */
	public Relation getType() {
		return this.type;
	}

	/**
	 * @return the from
	 */
	public IntentionNode<E> getFromNode() {
		return this.from;
	}

	/**
	 * @return the to
	 */
	public IntentionNode<E> getToNode() {
		return this.to;
	}
	
	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		ret.append(super.toString());
//		ret.append('(');
//		ret.append(from.getElem().getShortName());
//		ret.append(',');
//		ret.append(to.getElem().getShortName());
//		ret.append(')');
		ret.append(this.type);
		return ret.toString();
	}

}