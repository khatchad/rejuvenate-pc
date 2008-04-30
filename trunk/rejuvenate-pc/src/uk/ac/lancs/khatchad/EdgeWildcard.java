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
public class EdgeWildcard<E extends IElement> extends Wildcard<E> {

	/* (non-Javadoc)
	 * @see uk.ac.lancs.khatchad.Wildcard#asString()
	 */
	@Override
	public String toString() {
		return this.type.toString();
	}

	Relation type;
	
	/**
	 * @param elementAt
	 */
	public EdgeWildcard(IntentionEdge<IElement> edge) {
		this.type = edge.getType();
	}

	/* (non-Javadoc)
	 * @see uk.ac.lancs.khatchad.Wildcard#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if ( obj instanceof IntentionEdge && ((IntentionEdge)obj).getType() == this.type || obj instanceof Wildcard)
			return true;
		else
			return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.type.hashCode();
	}
}